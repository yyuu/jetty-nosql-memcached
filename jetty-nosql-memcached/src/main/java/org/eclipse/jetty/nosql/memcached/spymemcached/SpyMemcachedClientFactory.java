package org.eclipse.jetty.nosql.memcached.spymemcached;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.memcached.AbstractMemcachedClientFactory;

public class SpyMemcachedClientFactory extends AbstractMemcachedClientFactory {
	@Override
	public AbstractKeyValueStoreClient create(String serverString) {
		return new SpyMemcachedClient(serverString);
	}
}
