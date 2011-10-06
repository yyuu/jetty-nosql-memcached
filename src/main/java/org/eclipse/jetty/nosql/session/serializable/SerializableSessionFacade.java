package org.eclipse.jetty.nosql.session.serializable;

import org.eclipse.jetty.nosql.session.*;

public class SerializableSessionFacade extends AbstractSessionFacade {
	public SerializableSessionFacade() {
		sessionFactory = new SerializableSessionFactory();
		transcoder = new SerializableTranscoder();
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
}
