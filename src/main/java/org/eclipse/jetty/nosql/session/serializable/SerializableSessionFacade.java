package org.eclipse.jetty.nosql.session.serializable;

import org.eclipse.jetty.nosql.session.*;

public class SerializableSessionFacade extends AbstractSessionFacade {
	public SerializableSessionFacade() {
		sessionFactory = new SerializableSessionFactory();
		transcoder = new SerializableTranscoder();
	}
}
