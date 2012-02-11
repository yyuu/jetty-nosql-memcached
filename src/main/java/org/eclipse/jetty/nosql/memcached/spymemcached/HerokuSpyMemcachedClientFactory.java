package org.eclipse.jetty.nosql.memcached.spymemcached;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;

public class HerokuSpyMemcachedClientFactory extends BinarySpyMemcachedClientFactory {
	@Override
	public AbstractKeyValueStoreClient create(String serverString) {
		return new HerokuSpyMemcachedClient(serverString);
	}
}
