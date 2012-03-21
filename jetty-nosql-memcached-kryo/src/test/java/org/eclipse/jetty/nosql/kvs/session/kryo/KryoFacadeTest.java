package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.kvs.session.kryo.KryoSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;

public class KryoFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new KryoSessionFacade();
	}
}
