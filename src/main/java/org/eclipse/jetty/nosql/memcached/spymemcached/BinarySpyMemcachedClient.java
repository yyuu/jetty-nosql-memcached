package org.eclipse.jetty.nosql.memcached.spymemcached;

import net.spy.memcached.ConnectionFactoryBuilder;

public class BinarySpyMemcachedClient extends SpyMemcachedClient {
	public BinarySpyMemcachedClient() {
		super();
	}

	public BinarySpyMemcachedClient(String serverString) {
		super(serverString);
	}

	@Override
	protected ConnectionFactoryBuilder getConnectionFactoryBuilder() {
		ConnectionFactoryBuilder factoryBuilder = super.getConnectionFactoryBuilder();
		return factoryBuilder.setProtocol(ConnectionFactoryBuilder.Protocol.BINARY);
	}
}