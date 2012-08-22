package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.session.kryo.KryoSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;

/**
 * 
 * @author yyuu
 * backward compatibility tests for deprecated interface
 */
@SuppressWarnings("deprecation")
public class KryoFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new KryoSessionFacade();
	}
}
