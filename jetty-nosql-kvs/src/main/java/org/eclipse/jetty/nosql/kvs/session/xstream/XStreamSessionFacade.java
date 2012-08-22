package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFacade;
import org.eclipse.jetty.nosql.kvs.session.ISerializableSession;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;

public class XStreamSessionFacade extends AbstractSessionFacade {
	public XStreamSessionFacade() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public XStreamSessionFacade(ClassLoader cl) {
		sessionFactory = new XStreamSessionFactory();
		transcoder = new XStreamTranscoder(cl);
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
			session = tc.decode(raw, XStreamSession.class);
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return session;
	}

	@Override
	public void setClassLoader(ClassLoader cl) {
		XStreamTranscoder tc = new XStreamTranscoder(cl);
		transcoder = tc;
	}
}
