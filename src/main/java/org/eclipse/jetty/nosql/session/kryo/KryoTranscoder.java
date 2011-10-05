package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.session.ISerializationTranscoder;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;

public class KryoTranscoder implements ISerializationTranscoder {
	private Kryo kryo = null;
	public KryoTranscoder() {
		kryo = new Kryo();
		kryo.setRegistrationOptional(true);
	}

	public byte[] encode(Object obj) throws Exception {
		ObjectBuffer buf = new ObjectBuffer(kryo);
		byte[] raw = buf.writeObject(obj);
		return raw;
	}

	public <T> T decode(byte[] raw, Class<T> klass) throws Exception {
		ObjectBuffer buf = new ObjectBuffer(kryo);
		T obj = buf.readObject(raw, klass);
		return obj;
	}
}
