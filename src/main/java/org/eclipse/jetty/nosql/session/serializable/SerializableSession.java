package org.eclipse.jetty.nosql.session.serializable;

import java.io.Serializable;

import org.eclipse.jetty.nosql.session.AbstractSerializableSession;


public class SerializableSession extends AbstractSerializableSession implements Serializable {
	private static final long serialVersionUID = 8406865621253286071L;

	public SerializableSession() {
		setCreationTime(System.currentTimeMillis());
		setAccessed(getCreationTime());
	}
}