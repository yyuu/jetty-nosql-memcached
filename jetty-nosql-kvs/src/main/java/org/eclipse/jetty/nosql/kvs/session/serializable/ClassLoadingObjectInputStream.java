package org.eclipse.jetty.nosql.kvs.session.serializable;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ClassLoadingObjectInputStream extends ObjectInputStream {
	private ClassLoader classLoader = null;

	public ClassLoadingObjectInputStream() throws IOException {
		super();
		classLoader = Thread.currentThread().getContextClassLoader();
	}

	public ClassLoadingObjectInputStream(java.io.InputStream in) throws IOException {
		this(in, Thread.currentThread().getContextClassLoader());
	}

	public ClassLoadingObjectInputStream(java.io.InputStream in, ClassLoader cl) throws IOException {
		super(in);
		classLoader = cl;
	}

	@Override
	public Class<?> resolveClass (java.io.ObjectStreamClass cl) throws IOException, ClassNotFoundException {
		try {
			return Class.forName(cl.getName(), false, classLoader);
		} catch (ClassNotFoundException e) {
			return super.resolveClass(cl);
		}
	}
}
