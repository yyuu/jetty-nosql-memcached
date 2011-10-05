package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.session.ISerializableSession;
import org.eclipse.jetty.nosql.session.AbstractSessionFactory;

public class KryoSessionFactory extends AbstractSessionFactory {
	public ISerializableSession create() {
		return new KryoSession();
	}
}
