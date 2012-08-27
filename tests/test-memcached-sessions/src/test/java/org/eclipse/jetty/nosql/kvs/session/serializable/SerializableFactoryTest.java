package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractFactoryTest;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFactory;

public class SerializableFactoryTest extends AbstractFactoryTest {
	@Override
	public AbstractSessionFactory createFactory() {
		return new SerializableSessionFactory();
	}
}
