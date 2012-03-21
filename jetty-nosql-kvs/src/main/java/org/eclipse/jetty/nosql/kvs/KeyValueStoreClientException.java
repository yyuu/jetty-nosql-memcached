package org.eclipse.jetty.nosql.kvs;

public class KeyValueStoreClientException extends Exception {
	private static final long serialVersionUID = -5808726400597328283L;

	public KeyValueStoreClientException(String message) {
		super(message);
	}

	public KeyValueStoreClientException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeyValueStoreClientException(Throwable cause) {
		super(cause);
	}
}
