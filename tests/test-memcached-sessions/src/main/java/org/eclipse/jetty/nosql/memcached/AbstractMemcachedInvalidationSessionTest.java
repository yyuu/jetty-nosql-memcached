package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.*;
import org.junit.Test;

public abstract class AbstractMemcachedInvalidationSessionTest extends AbstractInvalidationSessionTest
{
    public AbstractTestServer createServer(int port)
    {
        MemcachedTestServer server = new MemcachedTestServer(port) {
            protected SessionCache getSessionCache(SessionHandler handler) {
                DefaultSessionCacheFactory cacheFactory = new DefaultSessionCacheFactory();
                cacheFactory.setEvictionPolicy(SessionCache.EVICT_ON_SESSION_EXIT);
                return cacheFactory.getSessionCache(handler);
            }
        };

        return server;
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
