package org.eclipse.jetty.nosql.kvs.session.kryo;

/**
 * @deprecated from 0.3.1. use #{@link KryoSessionFactory} instead.
 */
@Deprecated
public class KryoSessionFacade extends KryoSessionFactory {
	public KryoSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public KryoSessionFacade(ClassLoader cl) {
		super(cl);
	}
}
