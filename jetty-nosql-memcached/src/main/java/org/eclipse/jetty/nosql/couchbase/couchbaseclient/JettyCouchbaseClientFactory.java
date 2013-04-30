package org.eclipse.jetty.nosql.couchbase.couchbaseclient;

import org.eclipse.jetty.nosql.couchbase.AbstractCouchbaseClientFactory;

public class JettyCouchbaseClientFactory extends AbstractCouchbaseClientFactory {
	@Override
	public JettyCouchbaseClient create(String serverString) {
		return new JettyCouchbaseClient(serverString);
	}
}
