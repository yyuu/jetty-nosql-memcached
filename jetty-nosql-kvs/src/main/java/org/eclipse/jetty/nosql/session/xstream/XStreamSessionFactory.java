package org.eclipse.jetty.nosql.session.xstream;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * @deprecated
 * @author yyuu
 * moved to org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory
 */
public class XStreamSessionFactory extends org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFactory {
	private static Logger log = Log.getLogger("org.eclipse.jetty.nosql.session.xstream");
	public XStreamSessionFactory() {
		super();
		log.warn("DEPRECATED: " + this.getClass().getCanonicalName() +
				" has been moved to " + this.getClass().getSuperclass().getCanonicalName());
	}
}
