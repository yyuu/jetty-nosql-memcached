package org.eclipse.jetty.nosql.session.serializable;

import org.eclipse.jetty.nosql.session.*;
import org.eclipse.jetty.server.session.AbstractSession;

public class SerializableSessionFacade extends AbstractSessionFacade {
	SerializableSessionFactory sessionFactory = null;
	ISerializationTranscoder transcoder = null;

	public SerializableSessionFacade() {
		sessionFactory = new SerializableSessionFactory();
		transcoder = new SerializableTranscoder();
	}

	@Override
	public ISerializableSession create() {
		return sessionFactory.create();
	}

	@Override
	public ISerializableSession create(String sessionId) {
		return sessionFactory.create(sessionId);
	}

	@Override
	public ISerializableSession create(String sessionId, long created) {
		return sessionFactory.create(sessionId, created);
	}

	@Override
	public ISerializableSession create(AbstractSession session) {
		return sessionFactory.create(session);
	}

	@Override
	public byte[] pack(ISerializableSession session) {
		return pack(session, transcoder);
	}

	public byte[] pack(ISerializableSession session, ISerializationTranscoder tc) {
		byte[] raw = null;
		try {
			raw = tc.encode(session);
		} catch (Exception error) {
			System.err.println(getClass().getName().toString() + "#pack: " + error);
		}
		return raw;
	}

	@Override
	public ISerializableSession unpack(byte[] raw) {
		return unpack(raw, transcoder);
	}

	public ISerializableSession unpack(byte[] raw, ISerializationTranscoder tc) {
		ISerializableSession session = null;
		try {
			session = tc.decode(raw, SerializableSession.class);
		} catch (Exception error) {
			System.err.println(getClass().getName().toString() + "#unpack: " + error);
		}
		return session;
	}
}
