package org.eclipse.jetty.nosql.session;

import org.eclipse.jetty.nosql.session.kryo.KryoSession;
import org.eclipse.jetty.server.session.AbstractSession;

public abstract class AbstractSessionFacade {
	protected AbstractSessionFactory sessionFactory;
	protected ISerializationTranscoder transcoder;

	public AbstractSessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public ISerializationTranscoder getTranscoder() {
		return transcoder;
	}

	public ISerializableSession create() {
		return getSessionFactory().create();
	}

	public ISerializableSession create(String sessionId) {
		return getSessionFactory().create(sessionId);
	}

	public ISerializableSession create(String sessionId, long created) {
		return getSessionFactory().create(sessionId, created);
	}

	public ISerializableSession create(AbstractSession session) {
		return getSessionFactory().create(session);
	}

	public byte[] pack(ISerializableSession session) {
		return pack(session, getTranscoder());
	}

	public byte[] pack(ISerializableSession session, ISerializationTranscoder tc) throws TranscoderException {
		byte[] raw = null;
		try {
			raw = tc.encode(session);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return raw;
	}

	public ISerializableSession unpack(byte[] raw) {
		return unpack(raw, getTranscoder());
	}

	public ISerializableSession unpack(byte[] raw, ISerializationTranscoder tc) {
		ISerializableSession session = null;
		try {
			session = tc.decode(raw, KryoSession.class);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return session;
	}
}
