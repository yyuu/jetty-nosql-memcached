package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionManager;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MemcachedSessionManager extends KeyValueStoreSessionManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.MemcachedSessionManager");

	public MemcachedSessionManager() {
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