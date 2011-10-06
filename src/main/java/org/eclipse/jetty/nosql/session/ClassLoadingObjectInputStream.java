package org.eclipse.jetty.nosql.session;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ClassLoadingObjectInputStream extends ObjectInputStream {
	public ClassLoadingObjectInputStream(java.io.InputStream in) throws IOException {
		super(in);
	}

	public ClassLoadingObjectInputStream() throws IOException {
		super();
	}

	@Override
	public Class<?> resolveClass (java.io.ObjectStreamClass cl) throws IOException, ClassNotFoundException {
		try {
			return Class.forName(cl.getName(), false, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException e) {
			return super.resolveClass(cl);
		}
	}
}
