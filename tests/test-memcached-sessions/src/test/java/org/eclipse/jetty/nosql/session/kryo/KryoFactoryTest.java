package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractFactoryTest;
import org.eclipse.jetty.nosql.session.kryo.KryoSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

/**
 * 
 * @author yyuu
 * backward compatibility tests for deprecated interface
 */
@SuppressWarnings("deprecation")
public class KryoFactoryTest extends AbstractFactoryTest {
	@Override
	public AbstractSessionFactory createFactory() {
		return new KryoSessionFactory();
	}
}
