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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.nosql.NoSqlSessionManager;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import com.mongodb.DBObject;
//import org.omg.CORBA._IDLTypeStub;

//import com.mongodb.BasicDBObject;
//import com.mongodb.DBCollection;
//import com.mongodb.DBObject;
//import com.mongodb.MongoException;

import net.spy.memcached.MemcachedClient;

public class MemcachedSessionManager extends NoSqlSessionManager {
	// private static final Logger LOG =
	// Log.getLogger(MemcachedSessionManager.class);

	private final static Logger __log = Log
			.getLogger("org.eclipse.jetty.server.session");
	private static final Logger LOG = __log;

	/**
	 * the context id is only set when this class has been started
	 */
	private String _contextId = null;

	/* ------------------------------------------------------------ */
	public MemcachedSessionManager() {
		super();
	}

	/*------------------------------------------------------------ */
	@Override
	public void doStart() throws Exception {
		super.doStart();
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jetty.server.session.AbstractSessionManager#setSessionIdManager
	 * (org.eclipse.jetty.server.SessionIdManager)
	 */
//	@Override
//	public void setSessionIdManager(SessionIdManager idManager) {
//		super.setSessionIdManager(idManager);
//	}

	/* ------------------------------------------------------------ */
	@Override
	protected synchronized Object save(NoSqlSession session, Object version,
			boolean activateAfterSave) {
		try {
			__log.debug("MemcachedSessionManager:save:" + session);
			session.willPassivate();

			MemcachedSessionData data = new MemcachedSessionData(session);
			long newver = 0;
			// handle new or existing
			if (version == null) {
				// New session
				newver = 1;
			} else {
				newver = ((Long) version).intValue() + 1;
			}
			data.setVersion(newver);

			// handle valid or invalid
			if (!session.isValid()) {
				data.setInvalid(true);
				data.setInvalidated(System.currentTimeMillis());
			}

			((MemcachedSessionIdManager) _sessionIdManager).memcachedSet(session.getId(), data);
			__log.debug("MemcachedSessionManager:save:db.sessions.update("
					+ session.getId() + "," + data + ")");

			if (activateAfterSave)
				session.didActivate();

			return new Long(newver);
		} catch (Exception e) {
			LOG.warn(e);
		}
		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected Object refresh(NoSqlSession session, Object version) {
		__log.debug("MemcachedSessionManager:refresh " + session);

		// check if our in memory version is the same as what is on the disk
		if (version != null) {
			MemcachedSessionData data = ((MemcachedSessionIdManager) _sessionIdManager).memcachedGet(session.getClusterId());
			long saved = 0;
			if (data != null) {
				saved = data.getVersion();

				if (saved == ((Long) version).longValue()) {
					__log.debug("MemcachedSessionManager:refresh not needed");
					return version;
				}
				version = new Long(saved);
			}
		}

		// If we are here, we have to load the object
		MemcachedSessionData data = ((MemcachedSessionIdManager) _sessionIdManager).memcachedGet(session
				.getClusterId());

		// If it doesn't exist, invalidate
		if (data == null) {
			__log.debug("MemcachedSessionManager:refresh:marking invalid, no object");
			session.invalidate();
			return null;
		}

		// If it has been flagged invalid, invalidate
		boolean valid = !data.isInvalid();
		if (!valid) {
			__log.debug("MemcachedSessionManager:refresh:marking invalid, valid flag "
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
			LOG.warn(e);
		}

		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected synchronized NoSqlSession loadSession(String clusterId) {
		MemcachedSessionData data = ((MemcachedSessionIdManager) _sessionIdManager).memcachedGet(clusterId);

		__log.debug("MemcachedSessionManager:loaded " + data);

		if (data == null) {
			return null;
		}

		boolean valid = !data.isInvalid();
		if (!valid) {
			return null;
		}

		try {
			long version = data.getVersion();
			long created = data.getCreationTime();
			long accessed = data.getAccessedTime();
			NoSqlSession session = new NoSqlSession(this, created, accessed,
					clusterId, version);

			// get the attributes for the context
			Enumeration<String> attrs = data.getAttributeNames();

			__log.debug("MemcachedSessionManager:attrs: " + attrs);
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
			LOG.warn(e);
		}
		return null;
	}

	/*------------------------------------------------------------ */
	@Override
	protected boolean remove(NoSqlSession session) {
		__log.debug("MemcachedSessionManager:remove:session "
				+ session.getClusterId());

		/*
		 * Check if the session exists and if it does remove the context
		 * associated with this session
		 */
		return ((MemcachedSessionIdManager) _sessionIdManager).memcachedDelete(session.getClusterId());
	}

	/*------------------------------------------------------------ */
	@Override
	protected void invalidateSession(String idInCluster) {
		__log.debug("MemcachedSessionManager:invalidateSession:invalidating "
				+ idInCluster);

		super.invalidateSession(idInCluster);

		/*
		 * pull back the 'valid' value, we can check if its false, if is we
		 * don't need to reset it to false
		 */
		MemcachedSessionData data = ((MemcachedSessionIdManager) _sessionIdManager).memcachedGet(idInCluster);

		if (data != null && !data.isInvalid()) {
			data.setInvalid(true);
			data.setInvalidated(System.currentTimeMillis());
			((MemcachedSessionIdManager) _sessionIdManager).memcachedSet(idInCluster, data);
		}
	}
	
	protected MemcachedClient getConnection() {
		return ((MemcachedSessionIdManager) _sessionIdManager).getConnection();
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
}
