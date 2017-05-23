package org.eclipse.jetty.nosql.kvs.session;

import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;


public abstract class AbstractSessionFactory {

	protected final static Logger log = Log.getLogger(AbstractSessionFactory.class);

	protected ISerializationTranscoder transcoder;

	public AbstractSessionFactory(ISerializationTranscoder t) {
		transcoder = t;
	}

	public abstract AbstractSerializableSession create(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs);

	public ISerializationTranscoder getTranscoder() {
		return transcoder;
	}

	public byte[] pack(SessionData session) {
		return pack(session, getTranscoder());
	}

	public abstract byte[] pack(SessionData session, ISerializationTranscoder tc) throws TranscoderException;

	public SessionData unpack(byte[] raw) {
		return unpack(raw, getTranscoder());
	}

	public abstract SessionData unpack(byte[] raw, ISerializationTranscoder tc) throws TranscoderException;

	public abstract void setClassLoader(ClassLoader cl);
}
