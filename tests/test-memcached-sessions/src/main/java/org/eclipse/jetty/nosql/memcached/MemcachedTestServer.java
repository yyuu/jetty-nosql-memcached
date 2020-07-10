package org.eclipse.jetty.nosql.memcached;

// ========================================================================
// Copyright (c) 1996-2009 Mort Bay Consulting Pty. Ltd.
// Copyright (c) 2012 Geisha Tokyo Entarteinment, Inc.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================


import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFactory;
import org.eclipse.jetty.nosql.memcached.hashmap.HashMapClientFactory;
import org.eclipse.jetty.nosql.memcached.spymemcached.BinarySpyMemcachedClientFactory;
import org.eclipse.jetty.nosql.memcached.spymemcached.HerokuSpyMemcachedClientFactory;
import org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClientFactory;
import org.eclipse.jetty.nosql.memcached.xmemcached.BinaryXMemcachedClientFactory;
import org.eclipse.jetty.nosql.memcached.xmemcached.XMemcachedClientFactory;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.session.*;


/**
 * @version $Revision$ $Date$
 */
public class MemcachedTestServer extends AbstractTestServer
{
    protected DefaultSessionIdManager _idManager;
    protected AbstractSessionFactory _sessionFactory;
    protected boolean _saveAllAttributes = false; // false save dirty, true save all
    
    public MemcachedTestServer(int port)
    {
        this(port, 30, 10);
    }

    public MemcachedTestServer(int port, int maxInactivePeriod, int scavengePeriod)
    {
        this(port, maxInactivePeriod, scavengePeriod, System.getProperty("org.eclipse.jetty.nosql.memcached.servers"));
    }
    
    public MemcachedTestServer(int port, int maxInactivePeriod, int scavengePeriod, String sessionIdMgrConfig) {
        super(port, maxInactivePeriod, scavengePeriod, sessionIdMgrConfig);
    }
    
    public MemcachedTestServer(int port, int maxInactivePeriod, int scavengePeriod, boolean saveAllAttributes)
    {
        this(port, maxInactivePeriod, scavengePeriod);
        _saveAllAttributes = saveAllAttributes;
    }

    public SessionIdManager newSessionIdManager(Object config)
    {
        String configString;
        if (config == null) {
            configString = "127.0.0.1:11211";
        } else {
            configString = (String) config;
        }
        if ( _idManager != null )
        {
            try
            {
                _idManager.stop();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            _idManager.setWorkerName("node0");
            
            try
            {
                _idManager.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            
            return _idManager;
        }
        
        try
        {
            System.err.println("MemcachedTestServer:SessionIdManager:" + _maxInactivePeriod + "/" + _scavengePeriod);
            _idManager = new DefaultSessionIdManager(_server);
//            _idManager.setKeyPrefix("MemcachedTestServer::");
//            _idManager.setKeySuffix("::MemcachedTestServer");
            // to avoid stupid bugs of instance initialization...
//            _idManager.setDefaultExpiry(_idManager.getDefaultExpiry());
//            _idManager.setServerString(_idManager.getServerString());
//            _idManager.setTimeoutInMs(_idManager.getTimeoutInMs());
            _idManager.setWorkerName("node0");

            return _idManager;
        }
        catch (Exception e)
        {
            throw new IllegalStateException();
        }
    }

    public AbstractSessionFactory newSessionFactory() {
        return new SerializableSessionFactory();
    }

    public SessionHandler newSessionHandler() throws Exception {
        SessionHandler handler = new SessionHandler();
        SessionCache sc = getSessionCache(handler);
        sc.setSessionDataStore(getDataStore(handler));
        handler.setSessionCache(sc);
        return handler;
    }


     protected SessionCache getSessionCache(SessionHandler handler) {
        DefaultSessionCacheFactory cacheFactory = new DefaultSessionCacheFactory();
        cacheFactory.setEvictionPolicy(SessionCache.NEVER_EVICT);
        return cacheFactory.getSessionCache(handler);
    }

    private MemcachedSessionDataStore getDataStore(SessionHandler handler) throws Exception {
        AbstractMemcachedClientFactory clientFactory = getMemcachedClientFactory();
        String configString = "127.0.0.1:11211";
        MemcachedSessionDataStoreFactory factory = new MemcachedSessionDataStoreFactory();
        factory.setSessionFactory(newSessionFactory());
        factory.setClientFactory(clientFactory);
        factory.setServerString(configString);
        factory.setGracePeriodSec(_scavengePeriod);
        factory.setDefaultExpiry(_scavengePeriod);
        factory.setTimeoutInMs(1000);
        return (MemcachedSessionDataStore) factory.getSessionDataStore(handler);
    }
    
    public static void main(String... args) throws Exception
    {
        MemcachedTestServer server8080 = new MemcachedTestServer(8080);
        server8080.addContext("/").addServlet(SessionDump.class,"/");
        server8080.start();
        
        MemcachedTestServer server8081 = new MemcachedTestServer(8081);
        server8081.addContext("/").addServlet(SessionDump.class,"/");
        server8081.start();
        
        server8080.join();
        server8081.join();
    }

    public AbstractMemcachedClientFactory getMemcachedClientFactory() {
        String useMock = System.getProperty("org.eclipse.jetty.nosql.memcached.useMock", "true").trim().toLowerCase(); // backward compatibility
        String useBinary = System.getProperty("org.eclipse.jetty.nosql.memcached.useBinary", "false").trim().toLowerCase(); // backward compatibility
        String cfName = System.getProperty("org.eclipse.jetty.nosql.memcached.clientFactory", "default").trim().toLowerCase();
        AbstractMemcachedClientFactory clientFactory;
        if (cfName.contains("spy")) {
            if (cfName.contains("binary")) {
                if (cfName.contains("heroku")) {
                    clientFactory = new HerokuSpyMemcachedClientFactory();
                } else {
                    clientFactory = new BinarySpyMemcachedClientFactory();
                }
            } else {
                clientFactory = new SpyMemcachedClientFactory();
            }
        } else if (cfName.contains("xmemcached")) {
            if (cfName.contains("binary")) {
              if (cfName.contains("heroku")) {
                  clientFactory = new BinaryXMemcachedClientFactory(); // FIXME: create HerokuXMemcachedClientFactory
              } else {
                  clientFactory = new BinaryXMemcachedClientFactory();
              }
            } else {
                clientFactory = new XMemcachedClientFactory();
            }
        } else {
            if ("true".equals(useMock)) {
                clientFactory = new HashMapClientFactory();
            } else {
                if ("true".equals(useBinary)) {
                    clientFactory = new BinarySpyMemcachedClientFactory();
                } else {
                    clientFactory = new SpyMemcachedClientFactory();
                }
            }
        }
        return clientFactory;
    }
}
