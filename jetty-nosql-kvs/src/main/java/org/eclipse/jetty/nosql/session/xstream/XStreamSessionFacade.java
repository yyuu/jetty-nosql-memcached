package org.eclipse.jetty.nosql.session.xstream;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * @deprecated from 0.2.0. use #{@link org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory} instead.
 * @author yyuu
 * moved to org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory
 */
@Deprecated
public class XStreamSessionFacade extends org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory {
	private static Logger log = Log.getLogger("org.eclipse.jetty.nosql.session.xstream");
	public XStreamSessionFacade() {
		super();
		log.warn("DEPRECATED: " + this.getClass().getCanonicalName() +
				" has been moved to " + this.getClass().getSuperclass().getCanonicalName());
	}
}
