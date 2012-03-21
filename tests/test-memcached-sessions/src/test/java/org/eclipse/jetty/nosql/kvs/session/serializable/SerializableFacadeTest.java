package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFacade;

public class SerializableFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new SerializableSessionFacade();
	}
}
