package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractInvalidationSessionTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.junit.Test;

public abstract class AbstractMemcachedInvalidationSessionTest extends AbstractInvalidationSessionTest
{
    public AbstractTestServer createServer(int port)
    {
        return new MemcachedTestServer(port);
    }

    public void pause()
    {
    }

    @Test
    public void testInvalidation() throws Exception
    {
        super.testInvalidation();
    }
}
