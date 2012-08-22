package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class KryoLocalSessionScavengingTest extends AbstractMemcachedLocalSessionScavengingTest
{
    @Override
    public AbstractTestServer createServer(int port)
    {
        return new KryoMemcachedTestServer(port);
    }
}
