package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

public class KryoSessionFactory extends AbstractSessionFactory {
	public ISerializableSession create() {
		return new KryoSession();
	}
}
