package org.eclipse.jetty.nosql.couchbase;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class CouchbaseSessionManager extends KeyValueStoreSessionManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.couchbase.CouchbaseSessionManager");

	public CouchbaseSessionManager() {
		super();
	}

	@Override
	public void doStart() throws Exception {
		log.info("starting...");
		super.doStart();
		log.info("started.");
	}

	@Override
	public void doStop() throws Exception {
		log.info("stopping...");
		super.doStop();
		log.info("stopped.");
	}
}
