package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;
import org.eclipse.jetty.server.session.SessionData;

public class SerializableSessionFactory extends AbstractSessionFactory {
	public SerializableSessionFactory() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SerializableSessionFactory(ClassLoader cl) {
		super(new SerializableTranscoder(cl));
	}

	@Override
	public AbstractSerializableSession create(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs) {
		return new SerializableSession(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs);
	}

	@Override
	public byte[] pack(SessionData session, ISerializationTranscoder tc) throws TranscoderException {
		byte[] raw = null;
		try {
			raw = tc.encode(session);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return raw;
	}

	@Override
	public SessionData unpack(byte[] raw, ISerializationTranscoder tc) {
		SessionData session = null;
		try {
			session = tc.decode(raw, SerializableSession.class);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return session;
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		SerializableTranscoder tc = new SerializableTranscoder(cl);
		transcoder = tc;
	}
}
