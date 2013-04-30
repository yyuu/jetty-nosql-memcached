package org.eclipse.jetty.nosql.couchbase;

import java.io.IOException;

import org.eclipse.jetty.nosql.couchbase.couchbaseclient.JettyCouchbaseClient;
import org.eclipse.jetty.nosql.couchbase.couchbaseclient.JettyCouchbaseClientFactory;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreSessionIdManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

public class CouchbaseSessionIdManager extends KeyValueStoreSessionIdManager {
	private final static Logger log = Log.getLogger("org.eclipse.jetty.nosql.couchbase.CouchbaseSessionIdManager");
	private JettyCouchbaseClientFactory _clientFactory = null;
	private String _bucketName = "default";
	private String _bucketPassword = "";
	private String _fallbackBucketName = "default";
	private String _fallbackBucketPassword = "";
	private String _fallbackServerString = null;
	
	public void setBucketPassword(String _bucketPassword) {
		this._bucketPassword = _bucketPassword;
	}

	public void setBucketName(String _bucketName) {
		this._bucketName = _bucketName;
	}

	public void setFallbackBucketPassword(String _fallbackBucketPassword) {
		this._fallbackBucketPassword = _fallbackBucketPassword;
	}

	public void setFallbackBucketName(String _fallbackBucketName) {
		this._fallbackBucketName = _fallbackBucketName;
	}

	public void setFallbackServerString(String _serverString) {
		this._fallbackServerString = _serverString;
	}

	public CouchbaseSessionIdManager(Server server) throws IOException {
		this(server, "127.0.0.1:8091");
	}

	public CouchbaseSessionIdManager(Server server, String serverString) throws IOException {
		this(server, serverString, null);
	}

	public CouchbaseSessionIdManager(Server server, String serverString, JettyCouchbaseClientFactory cf) throws IOException {
		super(server, serverString);
		_clientFactory = cf;
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
	protected JettyCouchbaseClient newClient(String serverString) {
		synchronized(this) {
			if (_clientFactory == null) {
				_clientFactory = new JettyCouchbaseClientFactory(); // default client
			}
		}
		JettyCouchbaseClient client = _clientFactory.create(serverString);
		client.setTimeoutInMs(getTimeoutInMs());
		client.setBucketName(this._bucketName);
		client.setBucketPassword(this._bucketPassword);
		if(this._fallbackServerString != null){
			client.setFallbackServerString(this._fallbackServerString);
			client.setFallbackBucketName(this._fallbackBucketName);
			client.setFallbackBucketPassword(this._fallbackBucketPassword);
		}
		
		return client;
	}

	public AbstractCouchbaseClientFactory getClientFactory() {
		return _clientFactory;
	}

	public void setClientFactory(JettyCouchbaseClientFactory cf) {
		synchronized(this) {
			_clientFactory = cf;
		}
	}
}
