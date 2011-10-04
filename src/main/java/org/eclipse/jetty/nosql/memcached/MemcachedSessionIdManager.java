package org.eclipse.jetty.nosql.memcached;

//========================================================================
//Copyright (c) 2011 Intalio, Inc.
//------------------------------------------------------------------------
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//and Apache License v2.0 which accompanies this distribution.
//The Eclipse Public License is available at
//http://www.eclipse.org/legal/epl-v10.html
//The Apache License v2.0 is available at
//http://www.opensource.org/licenses/apache2.0.php
//You may elect to redistribute this code under either of these licenses.
//========================================================================

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.AbstractSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * Based partially on the jdbc session id manager...
 * 
 * Theory is that we really only need the session id manager for the local
 * instance so we have something to scavenge on, namely the list of known ids
 * 
 * this class has a timer that runs at the scavenge delay that runs a query for
 * all id's known to this node and that have and old accessed value greater then
 * the scavengeDelay.
 * 
 * these found sessions are then run through the invalidateAll(id) method that
 * is a bit hinky but is supposed to notify all handlers this id is now DOA and
 * ought to be cleaned up. this ought to result in a save operation on the
 * session that will change the valid field to false (this conjecture is
 * unvalidated atm)
 */
public class MemcachedSessionIdManager extends AbstractSessionIdManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.MemcachedSessionIdManager");

	private MemcachedClient _connection;
	protected Server _server;

	private long _scavengeDelay = TimeUnit.MINUTES.toMillis(30); // 30 minutes

	protected final Set<String> _sessions = Collections.synchronizedSet(new LinkedHashSet<String>());

	private String _serverString = "127.0.0.1:11211";
	private int _timeoutInMs = 1000;
	private String _keyPrefix = "";
	private String _keySuffix = "";
	private Transcoder<byte[]> tc = new NullTranscoder();

	/* ------------------------------------------------------------ */
	public MemcachedSessionIdManager(Server server) throws IOException {
		this(server, "127.0.0.1:11211");
	}

	/* ------------------------------------------------------------ */
	public MemcachedSessionIdManager(Server server, String serverString) throws IOException {
		super(new Random());
		this._server = server;
		this._serverString = serverString;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 * Scavenge is a process that periodically checks the tracked session ids of
	 * this given instance of the session id manager to see if they are past the
	 * point of expiration.
	 */
	protected void scavenge() {
		return;
	}

	/* ------------------------------------------------------------ */
	/**
	 * ScavengeFully is a process that periodically checks the tracked session
	 * ids of this given instance of the session id manager to see if they are
	 * past the point of expiration.
	 * 
	 * NOTE: this is potentially devastating and may lead to serious session
	 * coherence issues, not to be used in a running cluster
	 */
	protected void scavengeFully() {
		log.debug("scavengeFully");
		for (String id: _sessions) {
			// refresh cached data and test again.
			MemcachedSessionData data = getKey(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getAccessed() < 0) {
				continue;
			}
			log.info("scavenging valid " + id);
			invalidateAll(id);
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 * Purge is a process that cleans the memcached cluster of old sessions that
	 * are no longer valid.
	 * 
	 * There are two checks being done here:
	 * 
	 * - if the accessed time is older then the current time minus the purge
	 * invalid age and it is no longer valid then remove that session - if the
	 * accessed time is older then the current time minus the purge valid age
	 * then we consider this a lost record and remove it
	 * 
	 * NOTE: if your system supports long lived sessions then the purge valid
	 * age should be set to zero so the check is skipped.
	 * 
	 * The second check was added to catch sessions that were being managed on
	 * machines that might have crashed without marking their sessions as
	 * 'valid=false'
	 */
	protected void purge() {
		return;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Purge is a process that cleans the memcached cluster of old sessions that
	 * are no longer valid.
	 * 
	 */
	protected void purgeFully() {
		log.debug("purgeFully");
		for (String id: _sessions) {
			// refresh cached data and test again.
			MemcachedSessionData data = getKey(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getInvalidated() < 0) {
				continue;
			}
			if (!data.isValid()) {
				log.info("purging invalid " + id);
				deleteKey(id);
				_sessions.remove(id);
			}
		}
	}

	/* ------------------------------------------------------------ */
	public MemcachedClient getConnection() {
		if (_connection == null || !_connection.isAlive()) {
			try {
				this._connection = new MemcachedClient(AddrUtil.getAddresses(_serverString));
			} catch (IOException error) {
				log.warn("getConnection: unable to establish connection to " + _serverString);
			}
		}
		return _connection;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 */
	public boolean isPurgeEnabled() {
		return false;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 */
	public void setPurge(boolean purge) {
		return;
	}

	/* ------------------------------------------------------------ */
	/**
	 * sets the scavengeDelay
	 */
	public void setScavengeDelay(long scavengeDelay) {
		this._scavengeDelay = scavengeDelay;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 */
	public void setScavengePeriod(long scavengePeriod) {
		return;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 */
	public void setPurgeDelay(long purgeDelay) {
		return;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 */
	public long getPurgeInvalidAge() {
		return -1;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 * sets how old a session is to be persisted past the point it is no longer
	 * valid
	 */
	public void setPurgeInvalidAge(long purgeValidAge) {
		return;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 */
	public long getPurgeValidAge() {
		return -1;
	}

	/* ------------------------------------------------------------ */
	/**
	 * @deprecated
	 * sets how old a session is to be persist past the point it is considered
	 * no longer viable and should be removed
	 * 
	 * NOTE: set this value to 0 to disable purging of valid sessions
	 */
	public void setPurgeValidAge(long purgeValidAge) {
		return;
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void doStart() throws Exception {
		log.debug("starting");
		super.doStart();
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void doStop() throws Exception {
		if (_connection != null) {
			_connection.shutdown();
			_connection = null;
		}
		super.doStop();
	}

	/* ------------------------------------------------------------ */
	/**
	 * is the session id known to memcached, and is it valid
	 */
	public boolean idInUse(String idInCluster) {
		return ! addKey(idInCluster, new MemcachedSessionData(idInCluster));
	}

	/* ------------------------------------------------------------ */
	public void addSession(HttpSession session) {
		if (session == null) {
			return;
		}

		log.debug("addSession:" + session.getId());

		_sessions.add(session.getId());
	}

	/* ------------------------------------------------------------ */
	public void removeSession(HttpSession session) {
		if (session == null) {
			return;
		}

		_sessions.remove(session.getId());
	}

	/* ------------------------------------------------------------ */
	public void invalidateAll(String sessionId) {
		_sessions.remove(sessionId);
		// tell all contexts that may have a session object with this id to
		// get rid of them
		Handler[] contexts = _server.getChildHandlersByClass(ContextHandler.class);
		for (int i = 0; contexts != null && i < contexts.length; i++) {
			SessionHandler sessionHandler = (SessionHandler) ((ContextHandler) contexts[i])
					.getChildHandlerByClass(SessionHandler.class);
			if (sessionHandler != null) {
				SessionManager manager = sessionHandler.getSessionManager();
				if (manager != null && manager instanceof MemcachedSessionManager) {
					((MemcachedSessionManager) manager).invalidateSession(sessionId);
				}
			}
		}
	}

	/* ------------------------------------------------------------ */
	public String getClusterId(String nodeId) {
		if (nodeId == null) {
			return null;
		}
		int dot = nodeId.lastIndexOf('.');
		return (dot > 0) ? nodeId.substring(0, dot) : nodeId;
	}

	/* ------------------------------------------------------------ */
	public String getNodeId(String clusterId, HttpServletRequest request) {
		if (clusterId == null) {
			return null;
		}
		if (_workerName != null) {
			return clusterId + '.' + _workerName;
		}
		return clusterId;
	}
	
	protected String mangleKey(String key) {
		return _keyPrefix + key + _keySuffix;
	}

	protected MemcachedSessionData getKey(String idInCluster) {
		log.debug("get: id=" + idInCluster);
		MemcachedSessionData data = null;
		byte[] raw = null;
		try {
			Future<byte[]> f = getConnection().asyncGet(mangleKey(idInCluster), tc);
			raw = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to get key: id=" + idInCluster, error);
		}
		if (raw == null) {
			return null;
		}
		try {
			data = MemcachedSessionData.unpack(raw);
		} catch (Exception error) {
			String str = Arrays.toString(raw);
			log.warn("unable to unpack data get from memcached: id=" + idInCluster + ", raw=" + str + ", data=" + data, error);
		}
		return data;
	}
	
	protected boolean setKey(String idInCluster, MemcachedSessionData data) {
		return setKey(idInCluster, data, getDefaultExpiry());
	}

	protected boolean setKey(String idInCluster, MemcachedSessionData data, int expiry) {
		log.debug("set: id=" + idInCluster + ", expiry=" + expiry);
		boolean result = false;
		byte[] raw = null;
		try {
			raw = MemcachedSessionData.pack(data);
		} catch (Exception error) {
			log.warn("unable to pack data for set to memcached: id=" + idInCluster + ", data=" + data, error);
		}
		if (raw == null) {
			return false;
		}
		try {
			Future<Boolean> f = getConnection().set(mangleKey(idInCluster), expiry, raw, tc);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to set key: id=" + idInCluster + ", data=" + data, error);
		}
		return result;
	}

	protected boolean addKey(String idInCluster, MemcachedSessionData data) {
		return addKey(idInCluster, data, getDefaultExpiry());
	}

	protected boolean addKey(String idInCluster, MemcachedSessionData data, int expiry) {
		log.debug("add: id=" + idInCluster + ", expiry=" + expiry);
		boolean result = false;
		byte[] raw = null;
		try {
			raw = MemcachedSessionData.pack(data);
		} catch (Exception error) {
			log.warn("unable to pack data for add to memcached: id=" + idInCluster + ", data=" + data, error);
		}
		if (raw == null) {
			return false;
		}
		try {
			Future<Boolean> f = getConnection().add(mangleKey(idInCluster), expiry, raw, tc);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to add key: id=" + idInCluster + ", data=" + data, error);
		}
		return result;
	}

	protected boolean deleteKey(String idInCluster) {
		log.debug("delete: id=" + idInCluster);
		boolean result = false;
		try {
			Future<Boolean> f = getConnection().delete(mangleKey(idInCluster));
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to delete key: id=" + idInCluster, error);
		}
		return result;
	}

	protected Set<String> getSessions() {
		return Collections.unmodifiableSet(_sessions);
	}
	
	public String getServerString() {
		return _serverString;
	}

	public void setServerString(String serverString) {
		this._serverString = serverString;
	}

	public int getDefaultExpiry() {
		return (int) TimeUnit.MILLISECONDS.toSeconds(_scavengeDelay);
	}

	public void setDefaultExpiry(int defaultExpiry) {
		this._scavengeDelay = TimeUnit.SECONDS.toMillis(defaultExpiry);
	}

	public int getTimeoutInMs() {
		return _timeoutInMs;
	}

	public void setTimeoutInMs(int timeoutInMs) {
		this._timeoutInMs = timeoutInMs;
	}

	public String getKeyPrefix() {
		return _keyPrefix;
	}

	public void setKeyPrefix(String keyPrefix) {
		this._keyPrefix = keyPrefix;
	}

	public String getKeySuffix() {
		return _keySuffix;
	}

	public void setKeySuffix(String keySuffix) {
		this._keySuffix = keySuffix;
	}

}
