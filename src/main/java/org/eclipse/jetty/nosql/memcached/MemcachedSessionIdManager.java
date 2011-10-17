package org.eclipse.jetty.nosql.memcached;

import java.io.IOException;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionIdManager;
import org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MemcachedSessionIdManager extends KeyValueStoreSessionIdManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.MemcachedSessionIdManager");

	public MemcachedSessionIdManager(Server server) throws IOException {
		this(server, "127.0.0.1:11211");
	}

	public MemcachedSessionIdManager(Server server, String serverString) throws IOException {
		super(server, serverString);
	}

	@Override
	protected void doStart() throws Exception {
		log.info("starting...");
		super.doStart();
		log.info("started.");
	}

	@Override
	protected void doStop() throws Exception {
		log.info("stopping...");
		super.doStop();
		log.info("stopped.");
	}

	public String getServerString() {
		return ((SpyMemcachedClient)this._client).getServerString();
	}

	public void setServerString(String serverString) {
		((SpyMemcachedClient)this._client).setServerString(serverString);
	}

	public int getTimeoutInMs() {
		return ((SpyMemcachedClient)this._client).getTimeoutInMs();
	}

	public void setTimeoutInMs(int timeoutInMs) {
		((SpyMemcachedClient)this._client).setTimeoutInMs(timeoutInMs);
	}

	@Override
	protected KeyValueStoreClient newClient(String serverString) {
		return new SpyMemcachedClient(serverString);
	}
}
