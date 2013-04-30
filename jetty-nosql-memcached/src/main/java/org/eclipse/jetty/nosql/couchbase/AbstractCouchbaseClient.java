package org.eclipse.jetty.nosql.couchbase;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;

public abstract class AbstractCouchbaseClient extends AbstractKeyValueStoreClient {
	protected String _bucketName = "default";
	protected String _bucketPassword = "";
	protected String _fallbackBucketName = "default";
	protected String _fallbackBucketPassword = "";
	protected String _fallbackServerString = null;

	public void setBucketPassword(String _bucketPassword) {
		this._bucketPassword = _bucketPassword;
	}

	public void setBucketName(String _bucketName) {
		this._bucketName = _bucketName;
	}

	public void setFallbackBucketPassword(String _fallbackBucketPassword) {
		this._fallbackBucketPassword = _fallbackBucketPassword;
	}

	public void setFallbackBucketName(String _fallbackBucketName) {
		this._fallbackBucketName = _fallbackBucketName;
	}

	public AbstractCouchbaseClient(String serverString) {
		super(serverString);
	}

	public void setFallbackServerString(String _serverString) {
		this._fallbackServerString = prepareServerString(_serverString);
	}

	@Override
	public void setServerString(String _serverString) {
		String serverString = prepareServerString(_serverString);
		super.setServerString(serverString);
	}

	private String prepareServerString(String _serverString) {
		/**
		 * multiple server is handled by JettyCouchbaseClient
		 */
		return _serverString.trim();
	}
}