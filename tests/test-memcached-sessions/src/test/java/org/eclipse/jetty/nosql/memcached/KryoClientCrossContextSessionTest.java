package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class KryoClientCrossContextSessionTest extends AbstractMemcachedClientCrossContextSessionTest
{
    @Override
    public AbstractTestServer createServer(int port)
    {
        return new KryoMemcachedTestServer(port);
    }
}
