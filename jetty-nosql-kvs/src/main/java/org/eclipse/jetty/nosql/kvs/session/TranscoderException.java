package org.eclipse.jetty.nosql.kvs.session;

public class TranscoderException extends RuntimeException {
	private static final long serialVersionUID = 7772013267017548308L;
	
	public TranscoderException(String message) {
		super(message);
	}

	public TranscoderException(String message, Throwable cause) {
		super(message, cause);
	}

	public TranscoderException(Throwable cause) {
		super(cause);
	}
}
