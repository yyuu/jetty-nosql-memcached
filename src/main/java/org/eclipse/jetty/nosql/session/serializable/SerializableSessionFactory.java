package org.eclipse.jetty.nosql.session.serializable;

import org.eclipse.jetty.nosql.session.ISerializableSession;
import org.eclipse.jetty.nosql.session.AbstractSessionFactory;

public class SerializableSessionFactory extends AbstractSessionFactory {
	public ISerializableSession create() {
		return new SerializableSession();
	}
}
