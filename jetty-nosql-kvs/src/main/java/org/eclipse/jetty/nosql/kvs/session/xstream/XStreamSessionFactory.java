package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;
import org.eclipse.jetty.server.session.SessionData;

public class XStreamSessionFactory extends AbstractSessionFactory {
  public XStreamSessionFactory() {
    this(Thread.currentThread().getContextClassLoader());
  }

  public XStreamSessionFactory(ClassLoader cl) {
    super(new XStreamTranscoder(cl));
  }

  @Override
  public AbstractSerializableSession create(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs) {
    return new XStreamSession(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs);
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
  public SessionData unpack(byte[] raw, ISerializationTranscoder tc) throws TranscoderException {
    SessionData session = null;
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

