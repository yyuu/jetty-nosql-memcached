package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class LightLoadTest extends AbstractMemcachedLightLoadTest
{
    @Override
    public AbstractTestServer createServer(int port)
    {
        return new XStreamMemcachedTestServer(port);
    }
}
