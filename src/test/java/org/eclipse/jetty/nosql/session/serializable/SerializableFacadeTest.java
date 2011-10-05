package org.eclipse.jetty.nosql.session.serializable;

import org.eclipse.jetty.nosql.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.session.AbstractSessionFacade;

public class SerializableFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new SerializableSessionFacade();
	}
}
