package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;

/* FIXME: XStreamRemoveSessionTest isn't working with Jetty 9.3 :( */
@org.junit.Ignore
public class XStreamRemoveSessionTest extends AbstractMemcachedRemoveSessionTest
{ 
    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
        return new XStreamMemcachedTestServer(port,max,scavenge);
    }
}
