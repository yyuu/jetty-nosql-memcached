package org.eclipse.jetty.nosql.memcached;

import java.io.IOException;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.IKeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionIdManager;
import org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClient;
//import org.eclipse.jetty.nosql.memcached.xmemcached.XMemcachedClient;
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
		return getClient().getServerString();
	}

	public void setServerString(String serverString) {
		getClient().setServerString(serverString);
	}

	public int getTimeoutInMs() {
		return getClient().getTimeoutInMs();
	}

	public void setTimeoutInMs(int timeoutInMs) {
		getClient().setTimeoutInMs(timeoutInMs);
	}

	@Override
	protected IKeyValueStoreClient newClient(String serverString) {
		return new SpyMemcachedClient(serverString);
	}
//	@Override
//	protected IKeyValueStoreClient newClient(String serverString) {
//		return new XMemcachedClient(serverString);
//	}

	private AbstractKeyValueStoreClient getClient() {
		return (AbstractKeyValueStoreClient) this._client;
	}
}
