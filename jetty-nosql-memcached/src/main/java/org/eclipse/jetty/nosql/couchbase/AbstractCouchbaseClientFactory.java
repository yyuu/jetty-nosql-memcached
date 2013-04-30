package org.eclipse.jetty.nosql.couchbase;

public abstract class AbstractCouchbaseClientFactory {
	public abstract AbstractCouchbaseClient create(String serverString);
}
