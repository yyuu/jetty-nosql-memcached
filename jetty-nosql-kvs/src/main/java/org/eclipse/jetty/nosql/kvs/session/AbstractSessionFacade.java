package org.eclipse.jetty.nosql.kvs.session;

import org.eclipse.jetty.server.session.AbstractSession;

/**
 * @deprecated from 0.3.1. use #{@link AbstractSessionFactory} instead.
 */
@Deprecated
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

	public abstract byte[] pack(ISerializableSession session, ISerializationTranscoder tc) throws TranscoderException;

	public ISerializableSession unpack(byte[] raw) {
		return unpack(raw, getTranscoder());
	}

	public abstract ISerializableSession unpack(byte[] raw, ISerializationTranscoder tc) throws TranscoderException;

	public abstract void setClassLoader(ClassLoader cl);
}
