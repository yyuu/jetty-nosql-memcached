package org.eclipse.jetty.nosql.session;

import junit.framework.TestCase;

public abstract class AbstractFacadeTest extends TestCase {
	protected AbstractSessionFacade facade = null;
	public void setUp() throws Exception {
		facade = createFacade();
	}

	public abstract AbstractSessionFacade createFacade();

	public void testCreate() throws Exception {
		assertFalse(facade.create().equals(facade.create()));
		assertFalse(facade.create("session1").equals(facade.create("session2")));
	}

	public void testPackUnpack() throws Exception {
		ISerializableSession session1, session2;

		session1 = facade.create("session1");
		session1.setAttribute("foo", "foo value");
		session1.setAttribute("bar", 22222);

		byte[] raw = facade.pack(session1);
		session2 = facade.unpack(raw);
		assertNotNull(session2);

		assertEquals(session1.getId(), session2.getId());
		assertEquals(session1.getCreationTime(), session2.getCreationTime());
		assertEquals(session1.getAttributeMap(), session2.getAttributeMap());
	}
}
