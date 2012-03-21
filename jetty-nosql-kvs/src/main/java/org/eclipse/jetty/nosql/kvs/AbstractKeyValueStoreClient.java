package org.eclipse.jetty.nosql.kvs;

public abstract class AbstractKeyValueStoreClient implements IKeyValueStoreClient {
	protected String _serverString = null;
	protected int _timeoutInMs = 1000;

	public AbstractKeyValueStoreClient(String serverString) {
		setServerString(serverString);
	}

	public String getServerString() {
		return _serverString;
	}

	public void setServerString(String _serverString) {
		this._serverString = _serverString;
	}

	public int getTimeoutInMs() {
		return _timeoutInMs;
	}

	public void setTimeoutInMs(int _timeoutInMs) {
		this._timeoutInMs = _timeoutInMs;
	}
}
