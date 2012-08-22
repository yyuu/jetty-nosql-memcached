package org.eclipse.jetty.nosql.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractFacadeTest;
import org.eclipse.jetty.nosql.session.xstream.XStreamSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;

/**
 *
 * @author yyuu
 * backward compatibility tests for deprecated interface
 */
@SuppressWarnings("deprecation")
public class XStreamFacadeTest extends AbstractFacadeTest {
	@Override
	public AbstractSessionFacade createFacade() {
		return new XStreamSessionFacade();
	}
}
