package org.eclipse.jetty.nosql.kvs.session;

import junit.framework.TestCase;
import org.eclipse.jetty.server.session.SessionData;

public abstract class AbstractFactoryTest extends TestCase {
	protected AbstractSessionFactory factory = null;
	public void setUp() throws Exception {
		factory = createFactory();
	}

	public abstract AbstractSessionFactory createFactory();

	public void testCreate() throws Exception {
		assertFalse(factory.create("session1", "/contextA", "0.0.0.0", 1L, 1L, 1L, 10)
				.equals(factory.create("session2", "/contextB", "0.0.0.0", 2L, 2L, 2L, 20)));

        assertFalse(factory.create("session1", "/context", "0.0.0.0", 1L, 1L, 1L, 10)
                .equals(factory.create("session2", "/context", "0.0.0.0", 1L, 1L, 1L, 10)));
		assertFalse(factory.create("session", "/contextA", "0.0.0.0", 1L, 1L, 1L, 10)
				.equals(factory.create("session", "/contextB", "0.0.0.0", 1L, 1L, 1L, 10)));
		assertFalse(factory.create("session", "/context", "0.0.0.0", 1L, 1L, 1L, 10)
				.equals(factory.create("session", "/context", "0.0.0.1", 1L, 1L, 1L, 10)));
	}

	public void testPackUnpack() throws Exception {
		SessionData session1, session2;

		session1 = factory.create("session1", "/context", "0.0.0.0", 1L, 1L, 1L, 10);
		session1.setLastNode("lastNode1");
		session1.setAttribute("foo", "foo value");
		session1.setAttribute("bar", 22222);

		byte[] raw = factory.pack(session1);
		session2 = factory.unpack(raw);
		assertNotNull(session2);

		assertEquals(session1.getId(), session2.getId());
		assertEquals(session1.getContextPath(), session2.getContextPath());
		assertEquals(session1.getVhost(), session2.getVhost());
		assertEquals(session1.getCreated(), session2.getCreated());
		assertEquals(session1.getAccessed(), session2.getAccessed());
		assertEquals(session1.getLastAccessed(), session2.getLastAccessed());
		assertEquals(session1.getMaxInactiveMs(), session2.getMaxInactiveMs());
		assertEquals(session1.getAllAttributes(), session2.getAllAttributes());
	}
}
