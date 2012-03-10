package org.eclipse.jetty.nosql.memcached.xmemcached;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.memcached.AbstractMemcachedClientFactory;

public class XMemcachedClientFactory extends AbstractMemcachedClientFactory {
	@Override
	public AbstractKeyValueStoreClient create(String serverString) {
		return new XMemcachedClient(serverString);
	}
}
