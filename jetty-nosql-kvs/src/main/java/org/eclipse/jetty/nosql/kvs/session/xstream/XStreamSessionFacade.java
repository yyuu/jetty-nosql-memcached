package org.eclipse.jetty.nosql.kvs.session.xstream;

/**
 * @deprecated
 */
public class XStreamSessionFacade extends XStreamSessionFactory {
	public XStreamSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public XStreamSessionFacade(ClassLoader cl) {
		super(cl);
	}
}
