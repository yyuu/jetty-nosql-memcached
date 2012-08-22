package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;

public class KryoSessionFacade extends AbstractSessionFacade {
	public KryoSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public KryoSessionFacade(ClassLoader cl) {
		sessionFactory = new KryoSessionFactory();
		transcoder = new KryoTranscoder(cl);
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
	public ISerializableSession unpack(byte[] raw, ISerializationTranscoder tc) throws TranscoderException {
		ISerializableSession session = null;
		try {
			session = tc.decode(raw, KryoSession.class);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return session;
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		KryoTranscoder tc = new KryoTranscoder(cl);
		transcoder = tc;
	}
}
