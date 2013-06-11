package org.eclipse.jetty.nosql.kvs;

//========================================================================
//Copyright (c) 2011 Intalio, Inc.
//Copyright (c) 2012 Geisha Tokyo Entertainment, Inc.
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
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFactory;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.AbstractSession;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class KeyValueStoreSessionManager extends NoSqlSessionManager
{
    private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionManager");
    protected String _cookieDomain = getSessionCookieConfig().getDomain();
    protected String _cookiePath = getSessionCookieConfig().getPath();
    protected AbstractSessionFactory sessionFactory = null;

    /* ------------------------------------------------------------ */
    public KeyValueStoreSessionManager()
    {
        super();
    }

    /*------------------------------------------------------------ */
    @Override
    public void doStart() throws Exception
    {
        log.info("starting...");
        super.doStart();
        if (_cookieDomain == null)
        {
            String[] cookieDomains = getContextHandler().getVirtualHosts();
            // commented as api dropped in jetty9
            //if (cookieDomains == null || cookieDomains.length == 0)
            //{
            //    cookieDomains = getContextHandler().getConnectorNames();
            //}
            if (cookieDomains == null || cookieDomains.length == 0)
            {
                cookieDomains = new String[] {
                    "*"
                };
            }
            this._cookieDomain = cookieDomains[0];
        }
        if (_cookiePath == null)
        {
            String cookiePath = getContext().getContextPath();
            if (cookiePath == null || "".equals(cookiePath))
            {
                cookiePath = "*";
            }
            this._cookiePath = cookiePath;
        }
        if (sessionFactory == null)
        {
            sessionFactory = new SerializableSessionFactory();
        }
        try
        {
            // use context class loader during object deserialization.
            // thanks Daniel Peters!
            sessionFactory.setClassLoader(getContext().getClassLoader());
            log.info("use context class loader for session deserializer.");
            // FIXME: is there any safe way to refer context's class loader?
            // getContext().getClassLoader() may raise SecurityException.
            // this will be determine by policy configuration of JRE.
        }
        catch (SecurityException error)
        {
        }
        log.info("started.");
    }

    @Override
    public void doStop() throws Exception
    {
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
     * @see org.eclipse.jetty.server.session.AbstractSessionManager#setSessionIdManager
     * (org.eclipse.jetty.server.SessionIdManager)
     */
    @Override
    public void setSessionIdManager(final SessionIdManager idManager)
    {
        try
        {
            KeyValueStoreSessionIdManager kvsIdManager = (KeyValueStoreSessionIdManager) idManager;
            super.setSessionIdManager(kvsIdManager);
        }
        catch (ClassCastException error)
        {
            log.warn("unable to cast " + idManager.getClass() + " to " + KeyValueStoreSessionIdManager.class + ".");
            throw (error);
        }
    }

    /* ------------------------------------------------------------ */
    @Override
    protected Object save(final NoSqlSession session, final Object version, final boolean activateAfterSave)
    {
        try
        {
            log.debug("save:" + session);
            session.willPassivate();

            if (!session.isValid())
            {
                log.debug("save: skip saving invalidated session: id=" + session.getId());
                deleteKey(session.getId());
                return null;
            }

            ISerializableSession data;
            synchronized (session)
            {
                data = getSessionFactory().create(session);
            }
            data.setDomain(_cookieDomain);
            data.setPath(_cookiePath);
            long longVersion = 1; // default version for new sessions
            if (version != null)
            {
                longVersion = (Long) version + 1L;
            }
            data.setVersion(longVersion);

            try
            {
                if (!setKey(session.getId(), data))
                {
                    throw (new RuntimeException("unable to set key: data=" + data));
                }
            }
            catch (TranscoderException error)
            {
                throw (new IllegalArgumentException("unable to serialize session: id=" + session.getId() + ", data="
                    + data, error));
            }
            log.debug("save:db.sessions.update(" + session.getId() + "," + data + ")");

            if (activateAfterSave)
            {
                session.didActivate();
            }

            return longVersion;
        }
        catch (Exception e)
        {
            log.warn(e);
        }
        return null;
    }

    /*------------------------------------------------------------ */
    @Override
    protected Object refresh(final NoSqlSession session, Object version)
    {
        log.debug("refresh " + session);
        ISerializableSession data = null;
        try
        {
            data = getKey(session.getClusterId());
        }
        catch (TranscoderException error)
        {
            throw (new IllegalStateException("unable to deserialize session: id=" + session.getClusterId(), error));
        }
        // check if our in memory version is the same as what is on KVS
        if (version != null)
        {
            long saved = 0;
            if (data != null)
            {
                saved = data.getVersion();

                if (saved == (Long) version)
                {
                    log.debug("refresh not needed");
                    return version;
                }
                version = Long.valueOf(saved);
            }
        }

        // If it doesn't exist, invalidate
        if (data == null)
        {
            log.debug("refresh:marking invalid, no object");
            session.invalidate();
            return null;
        }

        // If it has been flagged invalid, invalidate
        boolean valid = data.isValid();
        if (!valid)
        {
            log.debug("refresh:marking invalid, valid flag " + valid);
            session.invalidate();
            return null;
        }

        // We need to update the attributes. We will model this as a passivate,
        // followed by bindings and then activation.
        session.willPassivate();
        try
        {
            for (Enumeration<String> e = data.getAttributeNames(); e.hasMoreElements();)
            {
                String name = e.nextElement();
                Object value = data.getAttribute(name);
                // only bind value if it didn't exist in session
                if (!session.getNames().contains(name))
                {
                    session.doPutOrRemove(name, value);
                    session.bindValue(name, value);
                }
                else
                {
                    session.doPutOrRemove(name, value);
                }
            }

            // cleanup, remove values from session, that don't exist in data anymore:
            for (String name : session.getNames())
            {
                if (!data.getAttributeMap().containsKey(name))
                {
                    session.doPutOrRemove(name, null);
                    session.unbindValue(name, session.getAttribute(name));
                }
            }

            session.didActivate();
            return version;
        }
        catch (Exception e)
        {
            log.warn(e);
        }

        return null;
    }

    @Override
    protected void addSession(final AbstractSession session)
    {
        // nop
    }

    @Override
    public AbstractSession getSession(final String idInCluster)
    {
        AbstractSession session;
        return loadSession(idInCluster);
    }

    /*------------------------------------------------------------ */
    @Override
    protected NoSqlSession loadSession(final String clusterId)
    {
        log.debug("loadSession: loading: id=" + clusterId);
        ISerializableSession data = getKey(clusterId);
        log.debug("loadSession: loaded: id=" + clusterId + ", data=" + data);

        if (data == null)
        {
            return null;
        }

        boolean valid = data.isValid();
        if (!valid)
        {
            log.debug("loadSession: id=" + clusterId + ", data=" + data + " has been invalidated.");
            return null;
        }

        if (!clusterId.equals(data.getId()))
        {
            log.warn("loadSession: invalid id (expected:" + clusterId + ", got:" + data.getId() + ")");
            return null;
        }

        synchronized (_cookieDomain)
        {
            if (_cookieDomain != null && !data.getDomain().equals("*") && !_cookieDomain.equals(data.getDomain()))
            {
                log.warn("loadSession: invalid cookie domain (expected:" + _cookieDomain + ", got:" + data.getDomain()
                    + ")");
                return null;
            }
        }

        synchronized (_cookiePath)
        {
            if (_cookiePath != null && !data.getPath().equals("*") && !_cookiePath.equals(data.getPath()))
            {
                log.warn("loadSession: invalid cookie path (expected:" + _cookiePath + ", got:" + data.getPath() + ")");
                return null;
            }
        }

        try
        {
            long version = data.getVersion();
            long created = data.getCreationTime();
            long accessed = data.getAccessed();
            NoSqlSession session = new NoSqlSession(this, created, accessed, clusterId, version);

            // get the attributes for the context
            Enumeration<String> attrs = data.getAttributeNames();

            //			log.debug("attrs: " + Collections.list(attrs));
            if (attrs != null)
            {
                while (attrs.hasMoreElements())
                {
                    String name = attrs.nextElement();
                    Object value = data.getAttribute(name);

                    session.doPutOrRemove(name, value);
                    session.bindValue(name, value);

                }
            }
            session.didActivate();

            return session;
        }
        catch (Exception e)
        {
            log.warn(e);
        }
        return null;
    }

    /*------------------------------------------------------------ */
    @Override
    protected boolean remove(final NoSqlSession session)
    {
        if (session == null)
        {
            return false;
        }
        else
        {
            return deleteKey(session.getId());
        }
    }

    @Override
    protected boolean removeSession(final String idInCluster)
    {
        return deleteKey(idInCluster);
    }

    /*------------------------------------------------------------ */
    @Override
    protected void invalidateSessions() throws Exception
    {
        // do nothing.
        // we do not want to invalidate all sessions on doStop().
        log.debug("invalidateSessions: nothing to do.");
    }

    /*------------------------------------------------------------ */
    @Override
    protected void invalidateSession(final String idInCluster)
    {
        // do nothing.
        // invalidated sessions will not save in KeyValueStoreSessionManager.save()
        log.debug("invalidateSession: invalidating " + idInCluster);
    }

    protected String mangleKey(final String idInCluster)
    {
        return idInCluster;
    }

    protected ISerializableSession getKey(final String idInCluster) throws TranscoderException
    {
        byte[] raw = ((KeyValueStoreSessionIdManager) _sessionIdManager).getKey(mangleKey(idInCluster));
        if (raw == null)
        {
            return null;
        }
        else
        {
            return getSessionFactory().unpack(raw);
        }
    }

    protected boolean setKey(final String idInCluster, final ISerializableSession data) throws TranscoderException
    {
        byte[] raw = getSessionFactory().pack(data);
        if (raw == null)
        {
            return false;
        }
        else
        {
            return ((KeyValueStoreSessionIdManager) _sessionIdManager).setKey(mangleKey(idInCluster), raw,
                getMaxInactiveInterval());
        }
    }

    protected boolean addKey(final String idInCluster, final ISerializableSession data) throws TranscoderException
    {
        byte[] raw = getSessionFactory().pack(data);
        if (raw == null)
        {
            return false;
        }
        else
        {
            return ((KeyValueStoreSessionIdManager) _sessionIdManager).addKey(mangleKey(idInCluster), raw,
                getMaxInactiveInterval());
        }
    }

    protected boolean deleteKey(final String idInCluster)
    {
        return ((KeyValueStoreSessionIdManager) _sessionIdManager).deleteKey(mangleKey(idInCluster));
    }

    /**
     * @deprecated from 0.3.1. use #{@link org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionManager#getSessionFactory()}
     *             instead.
     */
    @Deprecated
    public AbstractSessionFactory getSessionFacade()
    {
        return sessionFactory;
    }

    /**
     * @deprecated from 0.3.1. use #
     *             {@link KeyValueStoreSessionManager#setSessionFactory(org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory)}
     *             instead.
     */
    @Deprecated
    public void setSessionFacade(final AbstractSessionFactory sf)
    {
        log.warn("deprecated setter `setSessionFacade' was called. this will be removed in future release.");
        this.sessionFactory = sf;
    }

    public AbstractSessionFactory getSessionFactory()
    {
        return sessionFactory;
    }

    public void setSessionFactory(final AbstractSessionFactory sf)
    {
        this.sessionFactory = sf;
    }

    /**
     * @deprecated from 0.3.0. this is false by default and is not an option.
     */
    @Deprecated
    public void setSticky(final boolean sticky)
    { // TODO: remove
        log.warn("deprecated setter `setSticky' was called. this will be removed in future release.");
    }

    /**
     * @deprecated from 0.3.0. this is false by default and is not an option.
     */
    @Deprecated
    public boolean isSticky()
    { // TODO: remove
        return false;
    }

    @Override
    protected void update(final NoSqlSession session, final String newClusterId, final String newNodeId)
        throws Exception
    {
        ISerializableSession data = getKey(session.getClusterId());
        if (data == null)
        {
            log.warn("Couldn't get session data for old key {}", session.getClusterId());
            return;
        }
        deleteKey(session.getClusterId());
        setKey(newClusterId, data);
    }
}
