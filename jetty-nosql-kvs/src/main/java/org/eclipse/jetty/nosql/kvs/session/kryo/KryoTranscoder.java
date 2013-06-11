package org.eclipse.jetty.nosql.kvs.session.kryo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      Output output = new Output(stream);
      kryo.writeObject(output, obj);
      output.close();
      raw = stream.toByteArray();
    } catch (Exception error) {
      throw(new TranscoderException(error));
    }
    return raw;
  }

  public <T> T decode(byte[] raw, Class<T> klass) throws TranscoderException {
    T obj = null;
    try {
      ByteArrayInputStream stream = new ByteArrayInputStream(raw);
      Input input = new Input(stream);
      obj = kryo.readObject(input, klass);
    } catch (Exception error) {
      throw(new TranscoderException(error));
    }
    return obj;
  }
}
