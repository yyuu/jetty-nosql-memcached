package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class OrphanedSessionTest extends AbstractMemcachedOrphanedSessionTest
{
    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
       return new XStreamMemcachedTestServer(port,max,scavenge);
    }
}
