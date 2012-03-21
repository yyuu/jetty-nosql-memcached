package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractLocalSessionScavengingTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.junit.Test;

public abstract class AbstractMemcachedLocalSessionScavengingTest extends AbstractLocalSessionScavengingTest
{
    public AbstractTestServer createServer(int port)
    {
        return new MemcachedTestServer(port);
    }

    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge) {
        return new MemcachedTestServer(port, max, scavenge);
    }

    @Test
    public void testLocalSessionsScavenging() throws Exception
    {
        super.testLocalSessionsScavenging();
    }
}
