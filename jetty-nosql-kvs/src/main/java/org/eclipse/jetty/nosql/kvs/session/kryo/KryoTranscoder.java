package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoTranscoder implements ISerializationTranscoder {
  private Kryo kryo = null;

  public KryoTranscoder() {
    this(Thread.currentThread().getContextClassLoader());
  }

  public KryoTranscoder(ClassLoader cl) {
    kryo = new Kryo();
    kryo.setRegistrationRequired(false);
    kryo.setClassLoader(cl);
  }

  public byte[] encode(Object obj) throws TranscoderException {
    byte[] raw = null;
    try {
      Output output = new Output( );
      kryo.writeObject(output, obj);
      output.close();
      raw = output.getBuffer();
    } catch (Exception error) {
      throw(new TranscoderException(error));
    }
    return raw;
  }

  public <T> T decode(byte[] raw, Class<T> klass) throws TranscoderException {
    T obj = null;
    try {
      Input input = new Input(raw);
      obj = kryo.readObject(input, klass);
    } catch (Exception error) {
      throw(new TranscoderException(error));
    }
    return obj;
  }
}
