package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class ReentrantRequestSessionTest extends AbstractMemcachedReentrantRequestSessionTest
{
    @Override
    public AbstractTestServer createServer(int port)
    {
        XStreamMemcachedTestServer server = new XStreamMemcachedTestServer(port);
        // FIXME: cannot set in parent?
        swallowExceptions = !server.isFullTest() && !server.isStickyTest();
        return server;
    }
}
