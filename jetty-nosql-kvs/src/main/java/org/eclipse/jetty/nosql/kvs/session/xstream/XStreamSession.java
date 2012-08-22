package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;

public class XStreamSession extends AbstractSerializableSession {
	private static final long serialVersionUID = -4000990196099331646L;

	public XStreamSession() {
		setCreationTime(System.currentTimeMillis());
		setAccessed(getCreationTime());
	}
}
