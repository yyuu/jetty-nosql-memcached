package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

/**
 * @deprecated
 * @author yyuu
 * moved to org.eclipse.jetty.nosql.kvs.session.kryo.KryoSessionFactory
 */
public class KryoSessionFactory extends org.eclipse.jetty.nosql.kvs.session.kryo.KryoSessionFactory {
	private static Logger log = Log.getLogger("org.eclipse.jetty.nosql.session.kryo");
	public KryoSessionFactory() {
		super();
		log.warn("DEPRECATED: " + this.getClass().getCanonicalName() +
				" has been moved to " + this.getClass().getSuperclass().getCanonicalName());
	}
}
