package org.eclipse.jetty.nosql.session;

import org.eclipse.jetty.server.session.AbstractSession;

public abstract class AbstractSessionFacade {
	public abstract ISerializableSession create();

	public abstract ISerializableSession create(String sessionId);

	public abstract ISerializableSession create(String sessionId, long created);

	public abstract ISerializableSession create(AbstractSession session);

	public abstract byte[] pack(ISerializableSession session);

	public abstract ISerializableSession unpack(byte[] raw);
}
