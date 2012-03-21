package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;

public abstract class AbstractMemcachedClientFactory {
	public abstract AbstractKeyValueStoreClient create(String serverString);
}