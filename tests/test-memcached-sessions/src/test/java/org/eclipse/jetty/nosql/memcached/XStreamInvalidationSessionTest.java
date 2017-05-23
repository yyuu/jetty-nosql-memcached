package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.server.session.AbstractTestServer;
import org.eclipse.jetty.server.session.DefaultSessionCacheFactory;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;

public class XStreamInvalidationSessionTest extends AbstractMemcachedInvalidationSessionTest {
    @Override
    public AbstractTestServer createServer(int port) {
        XStreamMemcachedTestServer server = new XStreamMemcachedTestServer(port) {
            protected SessionCache getSessionCache(SessionHandler handler) {
                DefaultSessionCacheFactory cacheFactory = new DefaultSessionCacheFactory();
                cacheFactory.setEvictionPolicy(SessionCache.EVICT_ON_SESSION_EXIT);
                return cacheFactory.getSessionCache(handler);
            }
        };

        return server;
    }
}
