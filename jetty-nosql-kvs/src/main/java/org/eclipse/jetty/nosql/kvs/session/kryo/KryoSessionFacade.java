package org.eclipse.jetty.nosql.kvs.session.kryo;

/**
 * @deprecated
 */
public class KryoSessionFacade extends KryoSessionFactory {
	public KryoSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public KryoSessionFacade(ClassLoader cl) {
		super(cl);
	}
}
