package org.eclipse.jetty.nosql.kvs;

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
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFacade;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class KeyValueStoreSessionManager extends NoSqlSessionManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionManager");
	protected String _cookieDomain = getSessionCookieConfig().getDomain();
	protected String _cookiePath = getSessionCookieConfig().getPath();
	protected AbstractSessionFacade sessionFacade = null;
	protected boolean _sticky = true;

	/* ------------------------------------------------------------ */
	public KeyValueStoreSessionManager() {
		super();
	}

	/*------------------------------------------------------------ */
	@Override
	public void doStart() throws Exception {
		log.info("starting...");
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
		ClassLoader classLoader;
		try {
			// use context class loader during object deserialization.
			// thanks Daniel Peters!
			classLoader = getContext().getClassLoader();
			// FIXME: is there any safe way to refer context's class loader?
			// getContext().getClassLoader() may raise SecurityException.
			// this will be determine by policy configuration of JRE.
		} catch(SecurityException error) {
			log.warn("unable to load context class loader by the reason of " + error.toString() + ".");
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		if (classLoader != null) {
			sessionFacade.setClassLoader(classLoader);
		}
		log.info("started.");
	}

	@Override
	public void doStop() throws Exception {
		// override doStop() and invalidatedSessions() to skip invoking NoSqlSessionManager#invalidatedSeessions()
		// we do not want to invalidate all sessions on servlets restart.
		log.info("stopping...");
		super.doStop();
		log.info("stopped.");
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
			KeyValueStoreSessionIdManager kvsIdManager = (KeyValueStoreSessionIdManager) idManager;
			super.setSessionIdManager(kvsIdManager);
			setSticky(kvsIdManager.isSticky());
		} catch (ClassCastException error) {
			log.warn("unable to cast " + idManager.getClass() + " to " + KeyValueStoreSessionIdManager.class + ".");
			throw(error);
		}
	}

	/* ------------------------------------------------------------ */
	@Override
	protected Object save(NoSqlSession session, Object version, boolean activateAfterSave) {
		try {
			log.debug("save:" + session);
			session.willPassivate();

			ISerializableSession data;
			synchronized (session) {
				if (session.isValid()) {
					data = getSessionFacade().create(session);
				} else {
					data = getSessionFacade().create(session.getId(), session.getCreationTime());
					data.setValid(false);
				}
			}
			data.setDomain(_cookieDomain);
			data.setPath(_cookiePath);
			long longVersion = 1; // default version for new sessions
			if (version != null) {
				longVersion = (Long) version + 1L;
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

			return longVersion;
		} catch (Exception e) {
			log.warn(e);
		}
		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected Object refresh(NoSqlSession session, Object version) {
		log.debug("refresh " + session);
		ISerializableSession data = getKey(session.getClusterId());

		// check if our in memory version is the same as what is on KVS
		if (version != null) {
			long saved = 0;
			if (data != null) {
				saved = data.getVersion();

				if (saved == (Long) version) {
					log.debug("refresh not needed");
					return version;
				}
				version = Long.valueOf(saved);
			}
		}

		// If it doesn't exist, invalidate
		if (data == null) {
			log.debug("refresh:marking invalid, no object");
			session.invalidate();
			return null;
		}

		// If it has been flagged invalid, invalidate
		boolean valid = data.isValid();
		if (!valid) {
			log.debug("refresh:marking invalid, valid flag " + valid);
			session.invalidate();
			return null;
		}

		// We need to update the attributes. We will model this as a passivate,
		// followed by bindings and then activation.
		session.willPassivate();
		try {
			for (Enumeration<String> e = data.getAttributeNames(); e.hasMoreElements();) {
				String name = e.nextElement();
				Object value = data.getAttribute(name);
				// only bind value if it didn't exist in session
				if (!session.getNames().contains(name)) {
					session.doPutOrRemove(name, value);
					session.bindValue(name, value);
				} else {
					session.doPutOrRemove(name, value);
				}
			}

			// cleanup, remove values from session, that don't exist in data anymore:
			for (String name: session.getNames()) {
				if (!data.getAttributeMap().containsKey(name)) {
					session.doPutOrRemove(name, null);
					session.unbindValue(name,  session.getAttribute(name));
				}
			}

			session.didActivate();
			return version;
		} catch (Exception e) {
			log.warn(e);
		}

		return null;
	}

	@Override
	protected void addSession(AbstractSession session) {
		if (isSticky()) {
			super.addSession(session);
		}
	}
	
	@Override
	public AbstractSession getSession(String idInCluster)
	{
		AbstractSession session;
		if (isSticky()) {
			session = super.getSession(idInCluster);
		} else {
			session = loadSession(idInCluster);
		}
		return session;
	}

	/*------------------------------------------------------------ */
	@Override
	protected NoSqlSession loadSession(String clusterId) {
		log.debug("loadSession: loading: id=" + clusterId);
		ISerializableSession data = getKey(clusterId);
		log.debug("loadSession: loaded: id=" + clusterId + ", data=" + data);

		if (data == null) {
			return null;
		}

		boolean valid = data.isValid();
		if (!valid) {
			log.debug("loadSession: id=" + clusterId + ", data="+ data + " has been invalidated.");
			return null;
		}

		if (!clusterId.equals(data.getId())) {
			log.warn("loadSession: invalid id (expected:" + clusterId + ", got:" + data.getId() + ")");
			return null;
		}

		synchronized (_cookieDomain) {
			if (_cookieDomain != null && !data.getDomain().equals("*") && !_cookieDomain.equals(data.getDomain())) {
				log.warn("loadSession: invalid cookie domain (expected:" + _cookieDomain + ", got:" + data.getDomain() + ")");
				return null;
			}
		}

		synchronized (_cookiePath) {
			if (_cookiePath != null && !data.getPath().equals("*") && !_cookiePath.equals(data.getPath())) {
				log.warn("loadSession: invalid cookie path (expected:" + _cookiePath + ", got:" + data.getPath() + ")");
				return null;
			}
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
		if (isSticky()) {
			super.invalidateSessions();
		} else {
			// do nothing.
			// we do not want to invalidate all sessions on doStop().
			log.debug("invalidateSessions: nothing to do.");
		}
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

	protected String mangleKey(String idInCluster) {
		return idInCluster;
	}

	protected ISerializableSession getKey(String idInCluster) {
		byte[] raw = ((KeyValueStoreSessionIdManager)_sessionIdManager).getKey(mangleKey(idInCluster));
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
		return raw != null && ((KeyValueStoreSessionIdManager) _sessionIdManager).setKey(mangleKey(idInCluster), raw, expiry);
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
		return raw != null && ((KeyValueStoreSessionIdManager) _sessionIdManager).addKey(mangleKey(idInCluster), raw, expiry);
	}

	protected boolean deleteKey(String idInCluster) {
		return ((KeyValueStoreSessionIdManager)_sessionIdManager).deleteKey(mangleKey(idInCluster));
	}

	public AbstractSessionFacade getSessionFacade() {
		return sessionFacade;
	}

	public void setSessionFacade(AbstractSessionFacade sf) {
		this.sessionFacade = sf;
	}

	public void setSticky(boolean sticky) {
		this._sticky = sticky;
	}

	public boolean isSticky() {
		return _sticky;
	}
}
