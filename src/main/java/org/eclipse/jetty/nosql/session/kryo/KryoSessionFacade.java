package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.session.*;

public class KryoSessionFacade extends AbstractSessionFacade {
	public KryoSessionFacade() {
		sessionFactory = new KryoSessionFactory();
		transcoder = new KryoTranscoder();
	}
}
