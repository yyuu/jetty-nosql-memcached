package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class KryoNewSessionTest extends AbstractMemcachedNewSessionTest
{
    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
        return new KryoMemcachedTestServer(port,max,scavenge);
    }
}
