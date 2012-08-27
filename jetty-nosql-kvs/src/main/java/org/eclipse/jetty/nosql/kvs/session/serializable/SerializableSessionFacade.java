package org.eclipse.jetty.nosql.kvs.session.serializable;

/**
 * @deprecated
 */
public class SerializableSessionFacade extends SerializableSessionFactory {
	public SerializableSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SerializableSessionFacade(ClassLoader cl) {
		super(cl);
	}
}
