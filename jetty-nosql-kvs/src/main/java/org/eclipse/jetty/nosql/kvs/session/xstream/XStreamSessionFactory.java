package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;

public class XStreamSessionFactory extends AbstractSessionFactory {
	public ISerializableSession create() {
		return new XStreamSession();
	}
}
