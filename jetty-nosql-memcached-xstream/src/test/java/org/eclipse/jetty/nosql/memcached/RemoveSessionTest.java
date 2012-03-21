package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class RemoveSessionTest extends AbstractMemcachedRemoveSessionTest
{ 
    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
        XStreamMemcachedTestServer server = new XStreamMemcachedTestServer(port,max,scavenge);
        // FIXME: cannot set in parent?
        swallowExceptions = !server.isFullTest() && !server.isStickyTest();
        return server;
    }
}
