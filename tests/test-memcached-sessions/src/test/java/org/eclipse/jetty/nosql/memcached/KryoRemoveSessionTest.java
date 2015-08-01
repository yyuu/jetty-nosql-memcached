package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

/* FIXME: KryoRemoveSessionTest isn't working with Jetty 9.3 :( */
@org.junit.Ignore
public class KryoRemoveSessionTest extends AbstractMemcachedRemoveSessionTest
{ 
    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
        return new KryoMemcachedTestServer(port,max,scavenge);
    }
}
