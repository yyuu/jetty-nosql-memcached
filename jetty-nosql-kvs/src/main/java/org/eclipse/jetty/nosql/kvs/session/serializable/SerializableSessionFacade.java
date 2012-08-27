package org.eclipse.jetty.nosql.kvs.session.serializable;

/**
 * @deprecated from 0.3.1. use #{@link SerializableSessionFactory} instead.
 */
@Deprecated
public class SerializableSessionFacade extends SerializableSessionFactory {
	public SerializableSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SerializableSessionFacade(ClassLoader cl) {
		super(cl);
	}
}
