package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class ServerCrossContextSessionTest extends AbstractMemcachedServerCrossContextSessionTest
{
    @Override
    public AbstractTestServer createServer(int port)
    {
        return new XStreamMemcachedTestServer(port);
    }
}
