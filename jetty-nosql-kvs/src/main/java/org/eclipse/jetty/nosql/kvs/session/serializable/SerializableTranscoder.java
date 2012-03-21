package org.eclipse.jetty.nosql.kvs.session.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.TranscoderException;

public class SerializableTranscoder implements ISerializationTranscoder {
	private ClassLoader classLoader = null;

	public SerializableTranscoder() {
		this(Thread.currentThread().getContextClassLoader());
	}

	public SerializableTranscoder(ClassLoader cl) {
		classLoader = cl;
	}

	public byte[] encode(Object obj) throws TranscoderException {
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
			throw(new TranscoderException(error));
		}
		return raw;
	}

	@SuppressWarnings("unchecked")
	public <T> T decode(byte[] raw, Class<T> klass) throws TranscoderException {
		if (raw == null) {
			return null;
		}
		Object obj = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(raw);
			ObjectInputStream ois = new ClassLoadingObjectInputStream(bais, classLoader);
			obj = ois.readObject();
		} catch (Exception error) {
			throw(new TranscoderException(error));
		}
		return (T) obj;
	}
}
