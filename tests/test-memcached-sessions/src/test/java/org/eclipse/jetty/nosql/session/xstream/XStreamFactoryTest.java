package org.eclipse.jetty.nosql.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractFactoryTest;
import org.eclipse.jetty.nosql.session.xstream.XStreamSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

/**
 *
 * @author yyuu
 * backward compatibility tests for deprecated interface
 */
@SuppressWarnings("deprecation")
public class XStreamFactoryTest extends AbstractFactoryTest {
	@Override
	public AbstractSessionFactory createFactory() {
		return new XStreamSessionFactory();
	}
}
