package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;

public class XStreamFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new XStreamSessionFacade();
	}
}
