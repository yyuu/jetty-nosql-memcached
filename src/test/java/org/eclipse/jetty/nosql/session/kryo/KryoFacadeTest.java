package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.session.AbstractSessionFacade;

public class KryoFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new KryoSessionFacade();
	}
}
