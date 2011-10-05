package org.eclipse.jetty.nosql.session.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.eclipse.jetty.nosql.session.ISerializationTranscoder;

public class SerializableTranscoder implements ISerializationTranscoder {
	public byte[] encode(Object obj) throws Exception {
		if (obj == null) {
			return null;
		}
		byte[] raw = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			raw = baos.toByteArray();
		} catch (Exception error) {
			throw(error);
		}
		return raw;
	}

	@SuppressWarnings("unchecked")
	public <T> T decode(byte[] raw, Class<T> klass) throws Exception {
		if (raw == null) {
			return null;
		}
		Object obj = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(raw);
			ObjectInputStream ois = new ObjectInputStream(bais);
			obj = ois.readObject();
		} catch (Exception error) {
			throw(error);
		}
		return (T) obj;
	}
}
