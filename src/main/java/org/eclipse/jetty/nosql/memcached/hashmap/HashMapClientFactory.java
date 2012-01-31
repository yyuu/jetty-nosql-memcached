package org.eclipse.jetty.nosql.memcached.hashmap;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.memcached.AbstractMemcachedClientFactory;

public class HashMapClientFactory extends AbstractMemcachedClientFactory {

	@Override
	public AbstractKeyValueStoreClient create(String serverString) {
		return new HashMapClient(serverString);
	}

}
