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

import java.util.Enumeration;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.NoSqlSessionManager;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import org.eclipse.jetty.nosql.memcached.MemcachedSessionData;

public class RedisSessionManager extends NoSqlSessionManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.redis.RedisSessionManager");
	private String _cookieDomain = getSessionCookieConfig().getDomain();
	private String _cookiePath = getSessionCookieConfig().getPath();
	private int _cookieMaxAge = getSessionCookieConfig().getMaxAge();

	/* ------------------------------------------------------------ */
	public RedisSessionManager() {
		super();
	}

	/*------------------------------------------------------------ */
	@Override
	public void doStart() throws Exception {
		super.doStart();
		if (_cookieDomain == null) {
			String[] cookieDomains = getContextHandler().getVirtualHosts();
			if (cookieDomains == null || cookieDomains.length == 0) {
				cookieDomains = getContextHandler().getConnectorNames();
			}
			if (cookieDomains == null || cookieDomains.length == 0) {
				cookieDomains = new String[] { "*" };
			}
			this._cookieDomain = cookieDomains[0];
		}
		if (_cookiePath == null) {
			String cookiePath = getContext().getContextPath();
			if (cookiePath == null || "".equals(cookiePath)) {
				cookiePath = "*";
			}
			this._cookiePath = cookiePath;
		}
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jetty.server.session.AbstractSessionManager#setSessionIdManager
	 * (org.eclipse.jetty.server.SessionIdManager)
	 */
	@Override
	public void setSessionIdManager(SessionIdManager idManager) {
		try {
			super.setSessionIdManager((RedisSessionIdManager) idManager);
		} catch (ClassCastException error) {
			log.warn("unable to cast " + idManager.getClass() + " to " + RedisSessionIdManager.class + ".");
			throw(error);
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	protected synchronized Object save(NoSqlSession session, Object version,
			boolean activateAfterSave) {
		try {
			log.debug("save:" + session);
			session.willPassivate();

			MemcachedSessionData data = null;
			if (session.isValid()) {
				data = new MemcachedSessionData(session);
			} else {
				Log.warn("save: try to recover attributes of invalidated session: id=" + session.getId());
				data = getKey(session.getId());
				if (data == null) {
					data = new MemcachedSessionData(session.getId(), session.getCreationTime());
				}
				if (data.isValid()) {
					data.setValid(false);
				}
			}
			data.setDomain(_cookieDomain);
			data.setPath(_cookiePath);
			long longVersion = 1; // default version for new sessions
			if (version != null) {
				longVersion = ((Long)version).longValue() + 1L;
			}
			data.setVersion(longVersion);

			boolean success = setKey(session.getId(), data);
			if (!success) {
				throw(new RuntimeException("unable to set key: data=" + data));
			}
			log.debug("save:db.sessions.update(" + session.getId() + "," + data + ")");

			if (activateAfterSave) {
				session.didActivate();
			}

			return new Long(longVersion);
		} catch (Exception e) {
			log.warn(e);
		}
		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected Object refresh(NoSqlSession session, Object version) {
		log.debug("refresh " + session);

		// check if our in memory version is the same as what is on the disk
		if (version != null) {
			MemcachedSessionData data = getKey(session.getClusterId());
			long saved = 0;
			if (data != null) {
				saved = data.getVersion();

				if (saved == ((Long) version).longValue()) {
					log.debug("refresh not needed");
					return version;
				}
				version = new Long(saved);
			}
		}

		// If we are here, we have to load the object
		MemcachedSessionData data = getKey(session.getClusterId());

		// If it doesn't exist, invalidate
		if (data == null) {
			log.debug("refresh:marking invalid, no object");
			session.invalidate();
			return null;
		}

		// If it has been flagged invalid, invalidate
		boolean valid = data.isValid();
		if (!valid) {
			log.debug("refresh:marking invalid, valid flag "
					+ valid);
			session.invalidate();
			return null;
		}

		// We need to update the attributes. We will model this as a passivate,
		// followed by bindings and then activation.
		session.willPassivate();
		try {
			session.clearAttributes();

			for (Enumeration<String> e = data.getAttributeNames(); e
					.hasMoreElements();) {
				String name = e.nextElement();
				Object value = data.getAttribute(name);
				session.doPutOrRemove(name, value);
				session.bindValue(name, value);
			}
			session.didActivate();

			return version;
		} catch (Exception e) {
			log.warn(e);
		}

		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected synchronized NoSqlSession loadSession(String clusterId) {
		MemcachedSessionData data = getKey(clusterId);

		log.debug("loaded " + data);

		if (data == null) {
			return null;
		}

		boolean valid = data.isValid();
		if (!valid) {
			return null;
		}

		if (!clusterId.equals(data.getId())) {
			log.warn("loadSession: invalid id (expected:" + clusterId + ", got:" + data.getId() + ")");
			return null;
		}
		
		if (!data.getDomain().equals("*") && !_cookieDomain.equals(data.getDomain())) {
			log.warn("loadSession: invalid cookie domain (expected:" + _cookieDomain + ", got:" + data.getDomain() + ")");
			return null;
		}

		if (!data.getPath().equals("*") && !_cookiePath.equals(data.getPath())) {
			log.warn("loadSession: invalid cookie path (expected:" + _cookiePath + ", got:" + data.getPath() + ")");
			return null;
		}

		try {
			long version = data.getVersion();
			long created = data.getCreationTime();
			long accessed = data.getAccessed();
			NoSqlSession session = new NoSqlSession(this, created, accessed, clusterId, version);

			// get the attributes for the context
			Enumeration<String> attrs = data.getAttributeNames();

//			log.debug("attrs: " + Collections.list(attrs));
			if (attrs != null) {
				while (attrs.hasMoreElements()) {
					String name = attrs.nextElement();
					Object value = data.getAttribute(name);

					session.doPutOrRemove(name, value);
					session.bindValue(name, value);

				}
			}
			session.didActivate();

			return session;
		} catch (Exception e) {
			log.warn(e);
		}
		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected boolean remove(NoSqlSession session) {
		log.debug("remove:session " + session.getClusterId());

		/*
		 * Check if the session exists and if it does remove the context
		 * associated with this session
		 */
		return deleteKey(session.getClusterId());
	}

	/*------------------------------------------------------------ */
	@Override
	protected void invalidateSessions() throws Exception {
		// do nothing.
		// we do not want to invalidate all sessions on doStop().
	}

	/*------------------------------------------------------------ */
	@Override
	protected void invalidateSession(String idInCluster) {
		log.debug("invalidateSession:invalidating " + idInCluster);

		super.invalidateSession(idInCluster);

		/*
		 * pull back the 'valid' value, we can check if its false, if is we
		 * don't need to reset it to false
		 */
		MemcachedSessionData data = getKey(idInCluster);

		if (data != null && data.isValid()) {
			data.setValid(false);
			boolean success = setKey(idInCluster, data);
			if (!success) {
				throw(new RuntimeException("unable to set key: data=" + data));
			}
		}
	}

	public void purge() {
		((RedisSessionIdManager) _sessionIdManager).purge();
	}

	public void purgeFully() {
		((RedisSessionIdManager) _sessionIdManager).purgeFully();
	}

	public void scavenge() {
		((RedisSessionIdManager) _sessionIdManager).scavenge();
	}

	public void scavengeFully() {
		((RedisSessionIdManager) _sessionIdManager).scavengeFully();
	}

	/*------------------------------------------------------------ */
	/**
	 * returns the total number of session objects in the session store
	 * 
	 * the count() operation itself is optimized to perform on the server side
	 * and avoid loading to client side.
	 */
	public long getSessionStoreCount() {
		return ((RedisSessionIdManager)_sessionIdManager).getSessions().size();
	}

	protected String mangleKey(String idInCluster) {
		return idInCluster;
	}

	protected MemcachedSessionData getKey(String idInCluster) {
		return ((RedisSessionIdManager)_sessionIdManager).getKey(mangleKey(idInCluster));
	}

	protected boolean setKey(String idInCluster, MemcachedSessionData data) {
		if (_cookieMaxAge < 0) {
			// use idManager's default expiry if _cookieMaxAge is negative. (expiry must not be negative)
			return ((RedisSessionIdManager)_sessionIdManager).setKey(mangleKey(idInCluster), data);
		} else {
			int expiry = _cookieMaxAge * 2;
			return ((RedisSessionIdManager)_sessionIdManager).setKey(mangleKey(idInCluster), data, expiry);
		}
	}

	protected boolean addKey(String idInCluster, MemcachedSessionData data) {
		if (_cookieMaxAge < 0) {
			// use idManager's default expiry if _cookieMaxAge is negative. (expiry must not be negative)
			return ((RedisSessionIdManager)_sessionIdManager).addKey(mangleKey(idInCluster), data);
		} else {
			int expiry = _cookieMaxAge * 2;
			return ((RedisSessionIdManager)_sessionIdManager).addKey(mangleKey(idInCluster), data, expiry);
		}
	}

	protected boolean deleteKey(String idInCluster) {
		return ((RedisSessionIdManager)_sessionIdManager).deleteKey(mangleKey(idInCluster));
	}
}
