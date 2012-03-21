package org.eclipse.jetty.nosql.memcached;

import java.io.IOException;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionIdManager;
import org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClientFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class MemcachedSessionIdManager extends KeyValueStoreSessionIdManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.MemcachedSessionIdManager");
	private AbstractMemcachedClientFactory _clientFactory = null;

	public MemcachedSessionIdManager(Server server) throws IOException {
		this(server, "127.0.0.1:11211");
	}

	public MemcachedSessionIdManager(Server server, String serverString) throws IOException {
		this(server, serverString, null);
	}

	public MemcachedSessionIdManager(Server server, String serverString, AbstractMemcachedClientFactory cf) throws IOException {
		super(server, serverString);
		_clientFactory = cf != null ? cf : new SpyMemcachedClientFactory();
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

	@Override
	protected AbstractKeyValueStoreClient newClient(String serverString) {
		AbstractKeyValueStoreClient client = _clientFactory.create(serverString);
		client.setTimeoutInMs(getTimeoutInMs());
		return client;
	}
}
