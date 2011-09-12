package org.eclipse.jetty.nosql.redis;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.server.session.AbstractSessionIdManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.nosql.memcached.MemcachedSessionData;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;

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
public class RedisSessionIdManager extends AbstractSessionIdManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.redis.RedisSessionIdManager");

	private ShardedJedis _connection;
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

	private List<JedisShardInfo> _redisShards = new ArrayList<JedisShardInfo>();
	private int _defaultExpiry = 0; // never expire
	private int _timeoutInMs = 1000;
	private String _keyPrefix = "";
	private String _keySuffix = "";

	/* ------------------------------------------------------------ */
	public RedisSessionIdManager(Server server) throws IOException {
		this(server, "127.0.0.1:6379");
	}

	/* ------------------------------------------------------------ */
	public RedisSessionIdManager(Server server, String serverString) throws IOException {
		super(new Random());
		this._server = server;
		setRedisShards(serverString);
	}

	/* ------------------------------------------------------------ */
	/**
	 * Scavenge is a process that periodically checks the tracked session ids of
	 * this given instance of the session id manager to see if they are past the
	 * point of expiration.
	 */
	protected void scavenge() {
		log.debug("scavenge:called with delay" + _scavengeDelay);

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
			MemcachedSessionData data = getKey(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getAccessed() < 0 || t < data.getAccessed()) {
				continue;
			}
			log.info("scavenging valid " + id);
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
		log.debug("scavengeFully");
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
	 * Purge is a process that cleans the redis cluster of old sessions that
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
		log.debug("purge:called with invalid age " + _purgeInvalidAge);

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
			MemcachedSessionData data = getKey(id);
			if (data == null) {
				// record was disappeared during iteration.
				_sessions.remove(id);
				continue;
			}
			if (data.getInvalidated() < 0 || t < data.getInvalidated()) {
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
	/**
	 * Purge is a process that cleans the redis cluster of old sessions that
	 * are no longer valid.
	 * 
	 */
	protected void purgeFully() {
		log.debug("purgeFully");
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
	public ShardedJedis getConnection() {
		if (_connection == null) { // TODO: check connectivity of _connection
			this._connection = new ShardedJedis(_redisShards);
		}
		return this._connection;
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
		log.debug("starting");

		/*
		 * setup the scavenger thread
		 */
		if (_scavengeDelay > 0) {
			_scavengeTimer = new Timer(getClass().getSimpleName().toString() + "#scavenger", true);

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

				_scavengeTimer.schedule(_scavengerTask, _scavengeDelay, _scavengePeriod);
			}
		}

		/*
		 * if purging is enabled, setup the purge thread
		 */
		if (_purge) {
			_purgeTimer = new Timer(getClass().getSimpleName().toString() + "#purger", true);

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
			_connection.disconnect();
			_connection = null;
		}

		super.doStop();
	}

	/* ------------------------------------------------------------ */
	/**
	 * is the session id known to redis, and is it valid
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
				if (manager != null && manager instanceof RedisSessionManager) {
					((RedisSessionManager) manager).invalidateSession(sessionId);
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
		MemcachedSessionData data = null;
		ShardedJedis conn = null;
		try {
			conn = getConnection();
			byte[] raw = conn.get(mangleKey(idInCluster).getBytes());
			data = MemcachedSessionData.unpack(raw);
		} catch (Exception error) {
			log.warn("unable to get key: id=" + idInCluster, error);
//		} finally {
//			if (conn != null) {
//				conn.disconnect();
//			}
		}
		return data;
	}
	
	protected boolean setKey(String idInCluster, MemcachedSessionData data) {
		return setKey(idInCluster, data, _defaultExpiry);
	}

	protected boolean setKey(String idInCluster, MemcachedSessionData data, int expiry) {
		boolean result = false;
		ShardedJedis conn = null;
		try {
			conn = getConnection();
			byte[] raw = MemcachedSessionData.pack(data);
			String f = conn.setex(mangleKey(idInCluster).getBytes(), expiry, raw);
			result = "OK".equals(f);
		} catch (Exception error) {
			log.warn("unable to set key: id=" + idInCluster + ", data=" + data, error);
//		} finally {
//			if (conn != null) {
//				conn.disconnect();
//			}
		}
		return result;
	}

	protected boolean addKey(String idInCluster, MemcachedSessionData data) {
		return addKey(idInCluster, data, _defaultExpiry);
	}

	protected boolean addKey(String idInCluster, MemcachedSessionData data, int expiry) {
		boolean result = false;
		ShardedJedis conn = null;
		try {
			conn = getConnection();
			byte[] raw = MemcachedSessionData.pack(data);
			long f = conn.setnx(mangleKey(idInCluster).getBytes(), raw);
			result = f == 1;
		} catch (Exception error) {
			log.warn("unable to add key: id=" + idInCluster + ", data=" + data, error);
//		} finally {
//			if (conn != null) {
//				conn.disconnect();
//			}
		}
		return result;
	}

	protected boolean deleteKey(String idInCluster) {
		boolean result = false;
		ShardedJedis conn = null;
		try {
			conn = getConnection();
			long f = conn.del(mangleKey(idInCluster));
			result = f == 1;
		} catch (Exception error) {
			log.warn("unable to delete key: id=" + idInCluster, error);
//		} finally {
//			if (conn != null) {
//				conn.disconnect();
//			}
		}
		return result;
	}

	protected Set<String> getSessions() {
		return _sessions.keySet();
	}
	
	public synchronized void setRedisShards(String serverString) {
		String[] shardStrings = serverString.split(" +");
		_redisShards.clear();
		for (String shardString: shardStrings) {
			String[] parsed = shardString.split(":", 2);
			String host = "127.0.0.1";
			int port = 6379;
			if (0 < parsed.length) {
				host = parsed[0];
			}
			if (1 < parsed.length) {
				port = Integer.parseInt(parsed[1]); 
			}
			JedisShardInfo si = new JedisShardInfo(host, port);
			_redisShards.add(si);
		}
	}

	public int getDefaultExpiry() {
		return _defaultExpiry;
	}

	public void setDefaultExpiry(int defaultExpiry) {
		this._defaultExpiry = defaultExpiry;
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
