package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;

public class SerializableSessionFactory extends AbstractSessionFactory {
	public SerializableSessionFactory() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SerializableSessionFactory(ClassLoader cl) {
		super(new SerializableTranscoder(cl));
	}

	public ISerializableSession create() {
		return new SerializableSession();
	}

	@Override
	public byte[] pack(ISerializableSession session, ISerializationTranscoder tc) throws TranscoderException {
		byte[] raw = null;
		try {
			raw = tc.encode(session);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return raw;
	}

	@Override
	public ISerializableSession unpack(byte[] raw, ISerializationTranscoder tc) {
		ISerializableSession session = null;
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
