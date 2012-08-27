package org.eclipse.jetty.nosql.kvs.session.xstream;

/**
 * @deprecated from 0.3.1. use #{@link XStreamSessionFactory} instead.
 */
@Deprecated
public class XStreamSessionFacade extends XStreamSessionFactory {
	public XStreamSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public XStreamSessionFacade(ClassLoader cl) {
		super(cl);
	}
}
