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
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.AbstractSession;
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
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached");

	private MemcachedClient _connection;
	protected Server _server;
	private Timer _scavengeTimer;
	private Timer _purgeTimer;
	private TimerTask _scavengerTask;
	private TimerTask _purgeTask;

	private long _scavengeDelay = 30 * 60 * 1000; // every 30 minutes
	private long _scavengePeriod = 10 * 6 * 1000; // wait at least 10 minutes

	/**
	 * purge process is enabled by default
	 */
	private boolean _purge = true;

	/**
	 * purge process would run daily by default
	 */
	private long _purgeDelay = 24 * 60 * 60 * 1000; // every day

	/**
	 * how long do you want to persist sessions that are no longer valid before
	 * removing them completely
	 */
	private long _purgeInvalidAge = 24 * 60 * 60 * 1000; // default 1 day

	/**
	 * how long do you want to leave sessions that are still valid before
	 * assuming they are dead and removing them
	 */
	private long _purgeValidAge = 7 * 24 * 60 * 60 * 1000; // default 1 week

	protected final Map<String, SessionDataCache> _sessions = new ConcurrentHashMap<String, SessionDataCache>();
	protected class SessionDataCache {
		protected long _accessed = -1;
		protected long _invalidated = -1;
		
		protected SessionDataCache() {
			this._accessed = System.currentTimeMillis();
		}
		protected SessionDataCache(long accessed) {
			this._accessed = accessed;
		}
		
		protected void setAccessed(long accessed) {
			this._accessed = accessed;
		}
		
		protected long getAccessed() {
			return _accessed;
		}
		
		protected void setInvalidated(long invalidated) {
			this._invalidated = invalidated;
		}
		
		protected long getInvalidated() {
			return _invalidated;
		}
	}

	private String _memcachedServerString = "127.0.0.1:11211";
	private int _memcachedDefaultExpiry = 0; // never expire
	private int _memcachedTimeoutInMs = 1000;
	private String _memcachedKeyPrefix = "";
	private String _memcachedKeySuffix = "";

	/* ------------------------------------------------------------ */
	public MemcachedSessionIdManager(Server server) throws IOException {
		this(server, "127.0.0.1:11211");
	}

	/* ------------------------------------------------------------ */
	public MemcachedSessionIdManager(Server server, String serverString) throws IOException {
		super(new Random());
		this._server = server;
		this._memcachedServerString = serverString;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Scavenge is a process that periodically checks the tracked session ids of
	 * this given instance of the session id manager to see if they are past the
	 * point of expiration.
	 */
	protected void scavenge() {
		log.debug("SessionIdManager#scavenge:called with delay" + _scavengeDelay);

		/*
		 * run a query returning results that: - are in the known list of
		 * sessionIds - have an accessed time less then current time - the
		 * scavenger period
		 * 
		 * we limit the query to return just the __ID so we are not sucking back
		 * full sessions
		 */

		long t = System.currentTimeMillis() - _scavengeDelay;
		for (String id : _sessions.keySet()) {
			SessionDataCache cache = _sessions.get(id);
			if (cache == null) {
				// cached record was disappeared during iteration. 
				_sessions.remove(id);
				continue;
			}
			if (cache.getAccessed() < 0 || t < cache.getAccessed()) {
				continue;
			}
			// refresh cached data and test again.
			MemcachedSessionData data = memcachedGet(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getAccessed() < 0 || t < data.getAccessed()) {
				continue;
			}
			log.info("MemcachedSessionIdManager:scavenging valid " + id);
			invalidateAll(id);
		}
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
		log.debug("MemcachedSessionIdManager#scavengeFully");
		for (String id: _sessions.keySet()) {
			SessionDataCache cache = _sessions.get(id);
			if (cache == null) {
				// cached record was disappeared during iteration. 
				_sessions.remove(id);
				continue;
			}
			if (cache.getAccessed() < 0) {
				continue;
			}
			// refresh cached data and test again.
			MemcachedSessionData data = memcachedGet(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getAccessed() < 0) {
				continue;
			}
			log.info("MemcachedSessionIdManager:scavenging valid " + id);
			invalidateAll(id);
		}
	}

	/* ------------------------------------------------------------ */
	/**
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
		log.debug("MemcachedSessionIdManager#purge:called with invalid age " + _purgeInvalidAge);

		long t = System.currentTimeMillis() - _purgeInvalidAge;
		for (String id : _sessions.keySet()) {
			SessionDataCache cache = _sessions.get(id);
			if (cache == null) {
				// cached record was disappeared during iteration. 
				_sessions.remove(id);
				continue;
			}
			if (cache.getInvalidated() < 0 || t < cache.getInvalidated()) {
				continue;
			}
			// refresh cached data and test again.
			MemcachedSessionData data = memcachedGet(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getInvalidated() < 0 || t < data.getInvalidated()) {
				continue;
			}
			if (!data.isValid()) {
				log.info("MemcachedSessionIdManager:purging invalid " + id);
				memcachedDelete(id);
				_sessions.remove(id);
			}
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Purge is a process that cleans the memcached cluster of old sessions that
	 * are no longer valid.
	 * 
	 */
	protected void purgeFully() {
		log.debug("MemcachedSessionIdManager#purgeFully");
		for (String id: _sessions.keySet()) {
			SessionDataCache cache = _sessions.get(id);
			if (cache == null) {
				// cached record was disappeared during iteration. 
				_sessions.remove(id);
				continue;
			}
			if (cache.getInvalidated() < 0) {
				continue;
			}
			// refresh cached data and test again.
			MemcachedSessionData data = memcachedGet(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getInvalidated() < 0) {
				continue;
			}
			if (!data.isValid()) {
				log.info("MemcachedSessionIdManager:purging invalid " + id);
				memcachedDelete(id);
				_sessions.remove(id);
			}
		}
	}

	/* ------------------------------------------------------------ */
	public MemcachedClient getConnection() {
		if (_connection == null || !_connection.isAlive()) {
			try {
				this._connection = new MemcachedClient(AddrUtil.getAddresses(_memcachedServerString));
			} catch (IOException error) {
				log.warn("MemcachedSessionIdManager:getConnection: unable to establish connection to " + _memcachedServerString);
			}
		}
		return _connection;
	}

	/* ------------------------------------------------------------ */
	public boolean isPurgeEnabled() {
		return _purge;
	}

	/* ------------------------------------------------------------ */
	public void setPurge(boolean purge) {
		this._purge = purge;
	}

	/* ------------------------------------------------------------ */
	/**
	 * sets the scavengeDelay
	 */
	public void setScavengeDelay(long scavengeDelay) {
		this._scavengeDelay = scavengeDelay;
	}

	/* ------------------------------------------------------------ */
	public void setScavengePeriod(long scavengePeriod) {
		this._scavengePeriod = scavengePeriod;
	}

	/* ------------------------------------------------------------ */
	public void setPurgeDelay(long purgeDelay) {
		if (isRunning()) {
			throw new IllegalStateException();
		}

		this._purgeDelay = purgeDelay;
	}

	/* ------------------------------------------------------------ */
	public long getPurgeInvalidAge() {
		return _purgeInvalidAge;
	}

	/* ------------------------------------------------------------ */
	/**
	 * sets how old a session is to be persisted past the point it is no longer
	 * valid
	 */
	public void setPurgeInvalidAge(long purgeValidAge) {
		this._purgeInvalidAge = purgeValidAge;
	}

	/* ------------------------------------------------------------ */
	public long getPurgeValidAge() {
		return _purgeValidAge;
	}

	/* ------------------------------------------------------------ */
	/**
	 * sets how old a session is to be persist past the point it is considered
	 * no longer viable and should be removed
	 * 
	 * NOTE: set this value to 0 to disable purging of valid sessions
	 */
	public void setPurgeValidAge(long purgeValidAge) {
		this._purgeValidAge = purgeValidAge;
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void doStart() throws Exception {
		log.debug("MemcachedSessionIdManager:starting");

		/*
		 * setup the scavenger thread
		 */
		if (_scavengeDelay > 0) {
			_scavengeTimer = new Timer("MemcachedSessionIdScavenger", true);

			synchronized (this) {
				if (_scavengerTask != null) {
					_scavengerTask.cancel();
				}

				_scavengerTask = new TimerTask() {
					@Override
					public void run() {
						scavenge();
					}
				};

				_scavengeTimer.schedule(_scavengerTask, _scavengeDelay,
						_scavengePeriod);
			}
		}

		/*
		 * if purging is enabled, setup the purge thread
		 */
		if (_purge) {
			_purgeTimer = new Timer("MemcachedSessionIdPurger", true);

			synchronized (this) {
				if (_purgeTask != null) {
					_purgeTask.cancel();
				}
				_purgeTask = new TimerTask() {
					@Override
					public void run() {
						purge();
					}
				};
				_purgeTimer.schedule(_purgeTask, _purgeDelay);
			}
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	protected void doStop() throws Exception {
		if (_scavengeTimer != null) {
			_scavengeTimer.cancel();
			_scavengeTimer = null;
		}

		if (_purgeTimer != null) {
			_purgeTimer.cancel();
			_purgeTimer = null;
		}
		
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
		return ! memcachedAdd(idInCluster, new MemcachedSessionData(idInCluster));
	}

	/* ------------------------------------------------------------ */
	public void addSession(HttpSession session) {
		if (session == null) {
			return;
		}

		log.debug("MemcachedSessionIdManager:addSession:" + session.getId());

		SessionDataCache cache = _sessions.get(session.getId());
		if (cache == null) {
			cache = new SessionDataCache(((AbstractSession)session).getAccessed());
		}
		_sessions.put(session.getId(), cache);
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
		return _memcachedKeyPrefix + key + _memcachedKeySuffix;
	}

	protected MemcachedSessionData memcachedGet(String idInCluster) {
		MemcachedSessionData data = null;
		try {
			Future<Object> f = getConnection().asyncGet(mangleKey(idInCluster));
			byte[] raw = (byte[]) f.get(_memcachedTimeoutInMs,
					TimeUnit.MILLISECONDS);
			data = MemcachedSessionData.unpack(raw);
		} catch (Exception error) {
			log.warn("unable to get from memcached: id=" + idInCluster, error);
		}
		return data;
	}
	
	protected boolean memcachedSet(String idInCluster, MemcachedSessionData data) {
		return memcachedSet(idInCluster, data, _memcachedDefaultExpiry);
	}

	protected boolean memcachedSet(String idInCluster, MemcachedSessionData data, int expiry) {
		boolean result = false;
		try {
			byte[] raw = MemcachedSessionData.pack(data);
			Future<Boolean> f = getConnection().set(mangleKey(idInCluster), expiry, raw);
			result = f.get(_memcachedTimeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to set to memcached: id=" + idInCluster + ", data=" + data, error);
		}
		return result;
	}

	protected boolean memcachedAdd(String idInCluster, MemcachedSessionData data) {
		return memcachedAdd(idInCluster, data, _memcachedDefaultExpiry);
	}

	protected boolean memcachedAdd(String idInCluster, MemcachedSessionData data, int expiry) {
		boolean result = false;
		try {
			byte[] raw = MemcachedSessionData.pack(data);
			Future<Boolean> f = getConnection().add(mangleKey(idInCluster), expiry, raw);
			result = f.get(_memcachedTimeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to add to memcached: id=" + idInCluster + ", data=" + data, error);
		}
		return result;
	}

	protected boolean memcachedDelete(String idInCluster) {
		boolean result = false;
		try {
			Future<Boolean> f = getConnection().delete(mangleKey(idInCluster));
			result = f.get(_memcachedTimeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			log.warn("unable to delete from memcached: id=" + idInCluster, error);
		}
		return result;
	}

	protected Set<String> getSessions() {
		return _sessions.keySet();
	}
	
	public String getMemcachedServerString() {
		return _memcachedServerString;
	}

	public void setMemcachedServerString(String memcachedServerString) {
		this._memcachedServerString = memcachedServerString;
	}

	public int getMemcachedDefaultExpiry() {
		return _memcachedDefaultExpiry;
	}

	public void setMemcachedDefaultExpiry(int memcachedDefaultExpiry) {
		this._memcachedDefaultExpiry = memcachedDefaultExpiry;
	}

	public int getMemcachedTimeoutInMs() {
		return _memcachedTimeoutInMs;
	}

	public void setMemcachedTimeoutInMs(int memcachedTimeoutInMs) {
		this._memcachedTimeoutInMs = memcachedTimeoutInMs;
	}

	public String getMemcachedKeyPrefix() {
		return _memcachedKeyPrefix;
	}

	public void setMemcachedKeyPrefix(String memcachedKeyPrefix) {
		this._memcachedKeyPrefix = memcachedKeyPrefix;
	}

	public String getMemcachedKeySuffix() {
		return _memcachedKeySuffix;
	}

	public void setMemcachedKeySuffix(String memcachedKeySuffix) {
		this._memcachedKeySuffix = memcachedKeySuffix;
	}

}
