package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;

public abstract class AbstractMemcachedClient extends AbstractKeyValueStoreClient {
	public AbstractMemcachedClient(String serverString) {
		super(serverString);
	}

	@Override
	public void setServerString(String _serverString) {
		String serverString = prepareServerString(_serverString);
		super.setServerString(serverString);
	}

	public String prepareServerString(String _serverString) {
		StringBuilder serverString = new StringBuilder();
		for (String s: _serverString.trim().split("[\\s,]+")) {
			serverString.append((0 < serverString.length() ? " " : "") + s);
		}
		return serverString.toString();
	}
}