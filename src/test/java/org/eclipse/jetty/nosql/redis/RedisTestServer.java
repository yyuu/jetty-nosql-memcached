package org.eclipse.jetty.nosql.redis;

// ========================================================================
// Copyright (c) 1996-2009 Mort Bay Consulting Pty. Ltd.
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



import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.nosql.redis.RedisSessionIdManager;
import org.eclipse.jetty.nosql.redis.RedisSessionManager;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.eclipse.jetty.server.session.SessionHandler;


/**
 * @version $Revision$ $Date$
 */
public class RedisTestServer extends AbstractTestServer
{
    
    static RedisSessionIdManager _idManager;
    private boolean _saveAllAttributes = false; // false save dirty, true save all
    
    public RedisTestServer(int port)
    {
        super(port, 30, 10);
    }

    public RedisTestServer(int port, int maxInactivePeriod, int scavengePeriod)
    {
        super(port, maxInactivePeriod, scavengePeriod);
    }
    
    
    public RedisTestServer(int port, int maxInactivePeriod, int scavengePeriod, boolean saveAllAttributes)
    {
        super(port, maxInactivePeriod, scavengePeriod);
        
        _saveAllAttributes = saveAllAttributes;
    }

    public SessionIdManager newSessionIdManager()
    {
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
            
            _idManager.setScavengeDelay(_scavengePeriod * 1000);
            _idManager.setScavengePeriod(_maxInactivePeriod);
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
            System.err.println("RedisTestServer:SessionIdManager:" + _maxInactivePeriod + "/" + _scavengePeriod);
            _idManager = new RedisSessionIdManager(_server, "127.0.0.1:6379");
            
            _idManager.setScavengeDelay((int)TimeUnit.SECONDS.toMillis(_scavengePeriod));
            _idManager.setScavengePeriod(_maxInactivePeriod);
            _idManager.setDefaultExpiry(300);
            _idManager.setKeyPrefix("RedisTestServer::");
            
            return _idManager;
        }
        catch (Exception e)
        {
            throw new IllegalStateException();
        }
    }

    public SessionManager newSessionManager()
    {
        RedisSessionManager manager;
        try
        {
            manager = new RedisSessionManager();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        manager.setSavePeriod(1);
        manager.setStalePeriod(0);
        manager.setSaveAllAttributes(_saveAllAttributes);
        //manager.setScavengePeriod((int)TimeUnit.SECONDS.toMillis(_scavengePeriod));
        return manager;
    }

    public SessionHandler newSessionHandler(SessionManager sessionManager)
    {
        return new SessionHandler(sessionManager);
    }
    
    public static void main(String... args) throws Exception
    {
        RedisTestServer server8080 = new RedisTestServer(8080);
        server8080.addContext("/").addServlet(SessionDump.class,"/");
        server8080.start();
        
        RedisTestServer server8081 = new RedisTestServer(8081);
        server8081.addContext("/").addServlet(SessionDump.class,"/");
        server8081.start();
        
        server8080.join();
        server8081.join();
    }

}
