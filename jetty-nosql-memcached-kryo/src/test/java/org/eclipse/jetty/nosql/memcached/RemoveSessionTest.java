package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

public class RemoveSessionTest extends AbstractMemcachedRemoveSessionTest
{ 
    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
        KryoMemcachedTestServer server = new KryoMemcachedTestServer(port,max,scavenge);
        // FIXME: cannot set in parent?
        swallowExceptions = !server.isFullTest() && !server.isStickyTest();
        return server;
    }
}
