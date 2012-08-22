package org.eclipse.jetty.nosql.session.xstream;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * @deprecated
 * @author yyuu
 * moved to org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFacade
 */
public class XStreamSessionFacade extends org.eclipse.jetty.nosql.kvs.session.xstream.XStreamSessionFacade {
	private static Logger log = Log.getLogger("org.eclipse.jetty.nosql.session.xstream");
	public XStreamSessionFacade() {
		super();
		log.warn("DEPRECATED: " + this.getClass().getCanonicalName() +
				" has been moved to " + this.getClass().getSuperclass().getCanonicalName());
	}
}
