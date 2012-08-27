package org.eclipse.jetty.nosql.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractFactoryTest;
import org.eclipse.jetty.nosql.session.xstream.XStreamSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

/**
 *
 * @author yyuu
 * backward compatibility tests for deprecated interface
 */
@SuppressWarnings("deprecation")
public class XStreamFacadeTest extends AbstractFactoryTest {
	@Override
	public AbstractSessionFactory createFactory() {
		return new XStreamSessionFacade();
	}
}
