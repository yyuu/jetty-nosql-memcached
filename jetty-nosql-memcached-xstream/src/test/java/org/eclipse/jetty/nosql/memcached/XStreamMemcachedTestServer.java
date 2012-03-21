package org.eclipse.jetty.nosql.memcached;

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

import org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFacade;
import org.eclipse.jetty.nosql.memcached.MemcachedSessionManager;
import org.eclipse.jetty.server.SessionManager;

/**
 * @version $Revision$ $Date$
 */
public class XStreamMemcachedTestServer extends MemcachedTestServer
{
    public XStreamMemcachedTestServer(int port)
    {
        super(port);
    }

    public XStreamMemcachedTestServer(int port, int maxInactivePeriod, int scavengePeriod)
    {
        super(port, maxInactivePeriod, scavengePeriod);
    }
    
    
    public XStreamMemcachedTestServer(int port, int maxInactivePeriod, int scavengePeriod, boolean saveAllAttributes)
    {
        super(port, maxInactivePeriod, scavengePeriod, saveAllAttributes);
    }

    @Override
    public SessionManager newSessionManager()
    {
        MemcachedSessionManager manager;
        try
        {
            manager = new MemcachedSessionManager();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        
        manager.setSavePeriod(1);
        manager.setStalePeriod(0);
        manager.setSaveAllAttributes(_saveAllAttributes);
        manager.setSessionFacade(new XStreamSessionFacade());
        //manager.setScavengePeriod((int)TimeUnit.SECONDS.toMillis(_scavengePeriod));
        return manager;
    }
}
