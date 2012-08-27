package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractFactoryTest;
import org.eclipse.jetty.nosql.kvs.session.kryo.KryoSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

public class KryoFactoryTest extends AbstractFactoryTest {
	@Override
	public AbstractSessionFactory createFactory() {
		return new KryoSessionFactory();
	}
}
