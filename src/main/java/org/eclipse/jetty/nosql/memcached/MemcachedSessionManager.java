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
import org.eclipse.jetty.nosql.session.AbstractSessionFacade;
import org.eclipse.jetty.nosql.session.ISerializableSession;
import org.eclipse.jetty.nosql.session.TranscoderException;
import org.eclipse.jetty.nosql.session.serializable.SerializableSessionFacade;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MemcachedSessionManager extends NoSqlSessionManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.MemcachedSessionManager");
	private String _cookieDomain = getSessionCookieConfig().getDomain();
	private String _cookiePath = getSessionCookieConfig().getPath();
	private AbstractSessionFacade sessionFacade = null;

	/* ------------------------------------------------------------ */
	public MemcachedSessionManager() {
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
		if (sessionFacade == null) {
			sessionFacade = new SerializableSessionFacade();
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
			super.setSessionIdManager((MemcachedSessionIdManager) idManager);
		} catch (ClassCastException error) {
			log.warn("unable to cast " + idManager.getClass() + " to " + MemcachedSessionIdManager.class + ".");
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

			ISerializableSession data = null;
			if (session.isValid()) {
				data = getSessionFacade().create(session);
			} else {
				Log.warn("save: try to recover attributes of invalidated session: id=" + session.getId());
				data = getKey(session.getId());
				if (data == null) {
					data = getSessionFacade().create(session.getId(), session.getCreationTime());
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

			return Long.valueOf(longVersion);
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
			ISerializableSession data = getKey(session.getClusterId());
			long saved = 0;
			if (data != null) {
				saved = data.getVersion();

				if (saved == ((Long) version).longValue()) {
					log.debug("refresh not needed");
					return version;
				}
				version = Long.valueOf(saved);
			}
		}

		// If we are here, we have to load the object
		ISerializableSession data = getKey(session.getClusterId());

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
		ISerializableSession data = getKey(clusterId);

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
		ISerializableSession data = getKey(idInCluster);

		if (data != null && data.isValid()) {
			data.setValid(false);
			boolean success = setKey(idInCluster, data);
			if (!success) {
				throw(new RuntimeException("unable to set key: data=" + data));
			}
		}
	}

	/**
	 * @deprecated
	 */
	public void purge() {
		return;
	}

	/**
	 * @deprecated
	 */
	public void purgeFully() {
		return;
	}

	/**
	 * @deprecated
	 */
	public void scavenge() {
		return;
	}

	/**
	 * @deprecated
	 */
	public void scavengeFully() {
		return;
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

	protected ISerializableSession getKey(String idInCluster) {
		byte[] raw = ((MemcachedSessionIdManager)_sessionIdManager).getKey(mangleKey(idInCluster));
		if (raw == null) {
			return null;
		}
		ISerializableSession data = null;
		try {
			data = getSessionFacade().unpack(raw);
		} catch (TranscoderException error) {
			log.warn("unable to deserialize data: id=" + idInCluster, error);
		} catch (Exception error) {
			log.warn("unknown exception during deserilization: id=" + idInCluster, error);
		}
		return data;
	}

	protected boolean setKey(String idInCluster, ISerializableSession data) {
		int expiry = getMaxInactiveInterval();
		byte[] raw = null;
		try {
			raw = getSessionFacade().pack(data);
		} catch (TranscoderException error) {
			log.warn("unable to serialize data: id=" + idInCluster + ", data=" + data, error);
		} catch (Exception error) {
			log.warn("unknown exception during serialization: id=" + idInCluster + ", data=" + data, error);
		}
		if (raw == null) {
			return false;
		}
		if (expiry < 0) {
			// use idManager's default expiry if _cookieMaxAge is negative. (expiry must not be negative)
			return ((MemcachedSessionIdManager)_sessionIdManager).setKey(mangleKey(idInCluster), raw);
		} else {
			return ((MemcachedSessionIdManager)_sessionIdManager).setKey(mangleKey(idInCluster), raw, expiry);
		}
	}

	protected boolean addKey(String idInCluster, ISerializableSession data) {
		int expiry = getMaxInactiveInterval();
		byte[] raw = null;
		try {
			raw = getSessionFacade().pack(data);
		} catch (TranscoderException error) {
			log.warn("unable to serialize data: id=" + idInCluster + ", data=" + data, error);
		} catch (Exception error) {
			log.warn("unknown exception during serialization: id=" + idInCluster + ", data=" + data, error);
		}
		if (raw == null) {
			return false;
		}
		if (expiry < 0) {
			// use idManager's default expiry if _cookieMaxAge is negative. (expiry must not be negative)
			return ((MemcachedSessionIdManager)_sessionIdManager).addKey(mangleKey(idInCluster), raw);
		} else {
			return ((MemcachedSessionIdManager)_sessionIdManager).addKey(mangleKey(idInCluster), raw, expiry);
		}
	}

	protected boolean deleteKey(String idInCluster) {
		return ((MemcachedSessionIdManager)_sessionIdManager).deleteKey(mangleKey(idInCluster));
	}

	public AbstractSessionFacade getSessionFacade() {
		return sessionFacade;
	}

	public void setSessionFacade(AbstractSessionFacade sf) {
		this.sessionFacade = sf;
	}
}
