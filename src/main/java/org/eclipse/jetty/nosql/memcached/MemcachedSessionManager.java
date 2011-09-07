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

import java.util.Enumeration;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.NoSqlSessionManager;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MemcachedSessionManager extends NoSqlSessionManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached");
	private String _contextPath = null;

	/* ------------------------------------------------------------ */
	public MemcachedSessionManager() {
		super();
	}

	/*------------------------------------------------------------ */
	@Override
	public void doStart() throws Exception {
		super.doStart();
		String contextPath = getContext().getContextPath();
		if (contextPath == null || contextPath.length() == 0) {
			contextPath = "*";
		}
		this._contextPath = contextPath;
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
			super.setSessionIdManager((MemcachedSessionIdManager) idManager);
		} catch (ClassCastException error) {
			log.warn("unable to cast " + idManager.getClass() + " to " + MemcachedSessionIdManager.class.getClass() + ".");
			throw(error);
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	protected synchronized Object save(NoSqlSession session, Object version,
			boolean activateAfterSave) {
		try {
			log.debug("MemcachedSessionManager:save:" + session);
			session.willPassivate();

			MemcachedSessionData data = new MemcachedSessionData(session);
			data.setContextPath(_contextPath);
			long newver = 0;
			// handle new or existing
			if (version == null) {
				// New session
				newver = 1;
			} else {
				newver = ((Long) version).intValue() + 1;
			}
			data.setVersion(newver);

			memcachedSet(session.getId(), data); // TODO: check returned value
			log.debug("MemcachedSessionManager:save:db.sessions.update("
					+ session.getId() + "," + data + ")");

			if (activateAfterSave) {
				session.didActivate();
			}

			return new Long(newver);
		} catch (Exception e) {
			log.warn(e);
		}
		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected Object refresh(NoSqlSession session, Object version) {
		log.debug("MemcachedSessionManager:refresh " + session);

		// check if our in memory version is the same as what is on the disk
		if (version != null) {
			MemcachedSessionData data = memcachedGet(session.getClusterId());
			long saved = 0;
			if (data != null) {
				saved = data.getVersion();

				if (saved == ((Long) version).longValue()) {
					log.debug("MemcachedSessionManager:refresh not needed");
					return version;
				}
				version = new Long(saved);
			}
		}

		// If we are here, we have to load the object
		MemcachedSessionData data = memcachedGet(session.getClusterId());

		// If it doesn't exist, invalidate
		if (data == null) {
			log.debug("MemcachedSessionManager:refresh:marking invalid, no object");
			session.invalidate();
			return null;
		}

		// If it has been flagged invalid, invalidate
		boolean valid = data.isValid();
		if (!valid) {
			log.debug("MemcachedSessionManager:refresh:marking invalid, valid flag "
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
		MemcachedSessionData data = memcachedGet(clusterId);

		log.debug("MemcachedSessionManager:loaded " + data);

		if (data == null) {
			return null;
		}

		boolean valid = data.isValid();
		if (!valid) {
			return null;
		}

		if (!clusterId.equals(data.getId())) {
			log.warn("MemcachedSessionmanager#loadSession: invalid id (expected:" + clusterId + ", got:" + data.getId() + ")");
			return null;
		}

		if (!_contextPath.equals(data.getContextPath())) {
			log.warn("MemcachedSessionManager#loadSession: invalid context path (expected" + _contextPath + ", got:" + data.getContextPath());
			return null;
		}

		try {
			long version = data.getVersion();
			long created = data.getCreationTime();
			long accessed = data.getAccessed();
			NoSqlSession session = new NoSqlSession(this, created, accessed,
					clusterId, version);

			// get the attributes for the context
			Enumeration<String> attrs = data.getAttributeNames();

			log.debug("MemcachedSessionManager:attrs: " + attrs);
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
		log.debug("MemcachedSessionManager:remove:session "
				+ session.getClusterId());

		/*
		 * Check if the session exists and if it does remove the context
		 * associated with this session
		 */
		return memcachedDelete(session.getClusterId());
	}

	/*------------------------------------------------------------ */
	@Override
	protected void invalidateSession(String idInCluster) {
		log.debug("MemcachedSessionManager:invalidateSession:invalidating "
				+ idInCluster);

		super.invalidateSession(idInCluster);

		/*
		 * pull back the 'valid' value, we can check if its false, if is we
		 * don't need to reset it to false
		 */
		MemcachedSessionData data = memcachedGet(idInCluster);

		if (data != null && data.isValid()) {
			data.setValid(false);
			memcachedSet(idInCluster, data); // TODO: check returned value
		}
	}

	public void purge() {
		((MemcachedSessionIdManager) _sessionIdManager).purge();
	}

	public void purgeFully() {
		((MemcachedSessionIdManager) _sessionIdManager).purgeFully();
	}

	public void scavenge() {
		((MemcachedSessionIdManager) _sessionIdManager).scavenge();
	}

	public void scavengeFully() {
		((MemcachedSessionIdManager) _sessionIdManager).scavengeFully();
	}

	/*------------------------------------------------------------ */
	/**
	 * returns the total number of session objects in the session store
	 * 
	 * the count() operation itself is optimized to perform on the server side
	 * and avoid loading to client side.
	 */
	public long getSessionStoreCount() {
		return ((MemcachedSessionIdManager)_sessionIdManager).getSessions().size();
	}

	protected String mangleKey(String idInCluster) {
		return idInCluster;
	}

	protected MemcachedSessionData memcachedGet(String idInCluster) {
		return ((MemcachedSessionIdManager)_sessionIdManager).memcachedGet(mangleKey(idInCluster));
	}

	protected boolean memcachedSet(String idInCluster, MemcachedSessionData data) {
		return ((MemcachedSessionIdManager)_sessionIdManager).memcachedSet(mangleKey(idInCluster), data);
	}

	protected boolean memcachedAdd(String idInCluster, MemcachedSessionData data) {
		return ((MemcachedSessionIdManager)_sessionIdManager).memcachedAdd(mangleKey(idInCluster), data);
	}

	protected boolean memcachedDelete(String idInCluster) {
		return ((MemcachedSessionIdManager)_sessionIdManager).memcachedDelete(mangleKey(idInCluster));
	}
}
