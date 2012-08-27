package org.eclipse.jetty.nosql.kvs.session;

import junit.framework.TestCase;

public abstract class AbstractFactoryTest extends TestCase {
	protected AbstractSessionFactory factory = null;
	public void setUp() throws Exception {
		factory = createFactory();
	}

	public abstract AbstractSessionFactory createFactory();

	public void testCreate() throws Exception {
		assertFalse(factory.create().equals(factory.create()));
		assertFalse(factory.create("session1").equals(factory.create("session2")));
	}

	public void testPackUnpack() throws Exception {
		ISerializableSession session1, session2;

		session1 = factory.create("session1");
		session1.setAttribute("foo", "foo value");
		session1.setAttribute("bar", 22222);

		byte[] raw = factory.pack(session1);
		session2 = factory.unpack(raw);
		assertNotNull(session2);

		assertEquals(session1.getId(), session2.getId());
		assertEquals(session1.getCreationTime(), session2.getCreationTime());
		assertEquals(session1.getAttributeMap(), session2.getAttributeMap());
	}
}
