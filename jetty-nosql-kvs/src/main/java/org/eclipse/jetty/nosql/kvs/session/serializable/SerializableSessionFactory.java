package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;

public class SerializableSessionFactory extends AbstractSessionFactory {
	public ISerializableSession create() {
		return new SerializableSession();
	}
}
