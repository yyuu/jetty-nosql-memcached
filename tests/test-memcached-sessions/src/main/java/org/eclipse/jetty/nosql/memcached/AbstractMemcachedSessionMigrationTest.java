package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractSessionMigrationTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.junit.Test;

public abstract class AbstractMemcachedSessionMigrationTest extends AbstractSessionMigrationTest
{
    public AbstractTestServer createServer(int port)
    {
        return new MemcachedTestServer(port);
    }

    @Test
    public void testSessionMigration() throws Exception
    {
        super.testSessionMigration();
    }
}
