package org.eclipse.jetty.nosql.memcached.spymemcached;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;

public class BinarySpyMemcachedClientFactory extends SpyMemcachedClientFactory {
	@Override
	public AbstractKeyValueStoreClient create(String serverString) {
		return new BinarySpyMemcachedClient(serverString);
	}
}
