package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractFactoryTest;
import org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

public class XStreamFactoryTest extends AbstractFactoryTest {
	@Override
	public AbstractSessionFactory createFactory() {
		return new XStreamSessionFactory();
	}
}
