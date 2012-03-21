package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractImmortalSessionTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.junit.Test;

public abstract class AbstractMemcachedImmortalSessionTest extends AbstractImmortalSessionTest
{
    public AbstractTestServer createServer(int port)
    {
        return new MemcachedTestServer(port);
    }

    @Test
    public void testImmortalSession() throws Exception
    {
        super.testImmortalSession();
    }
}
