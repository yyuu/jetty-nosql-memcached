package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class ClientCrossContextSessionTest extends AbstractMemcachedClientCrossContextSessionTest
{
    @Override
    public AbstractTestServer createServer(int port)
    {
        return new XStreamMemcachedTestServer(port);
    }
}
