package org.eclipse.jetty.nosql.couchbase.couchbaseclient;

import java.net.SocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.nosql.couchbase.AbstractCouchbaseClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;
import org.eclipse.jetty.nosql.memcached.spymemcached.NullTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.spy.memcached.ConnectionObserver;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.FailureMode;
import net.spy.memcached.transcoders.Transcoder;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.CouchbaseConnectionFactory;
import com.couchbase.client.CouchbaseConnectionFactoryBuilder;

public class JettyCouchbaseClient extends AbstractCouchbaseClient {
	private static final int FOREVER = 0;
	private CouchbaseClient _client = null;
	private CouchbaseClient _fallbackClient = null;
	private Transcoder<byte[]> _transcoder = null;
	private static Logger log = LoggerFactory.getLogger("JettyCouchbaseClient");

	public JettyCouchbaseClient() {
		this("127.0.0.1:8091");
	}

	public JettyCouchbaseClient(String serverString) {
		super(serverString);
		this._transcoder = new NullTranscoder();
	}

	private CouchbaseClient connectToCouchbase(String serversString, String bucketName, String bucketPassword) throws KeyValueStoreClientException{
		if(serversString == null)
			return null;
		CouchbaseClient client;
		try {
	    	String[] serverArray = serversString.split(" ");
			List<URI> uris = new LinkedList<URI>();
			for(String server : serverArray){
				if(server == "" || server == null) break;
				uris.add(URI.create("http://"+server+"/pools"));
			}
	        CouchbaseConnectionFactory cf;
	        CouchbaseConnectionFactoryBuilder cfb = new CouchbaseConnectionFactoryBuilder();
	        cfb.setFailureMode(FailureMode.Redistribute);
	        cfb.setMaxReconnectDelay(30);
	        cfb.setReadBufferSize(16384);
	        cfb.setOpTimeout(5000);
	        cfb.setOpQueueMaxBlockTime(10000);
	        cfb.setProtocol(CouchbaseConnectionFactoryBuilder.Protocol.BINARY); 
	        cfb.setLocatorType(CouchbaseConnectionFactoryBuilder.Locator.CONSISTENT); 
	        cfb.setHashAlg(DefaultHashAlgorithm.KETAMA_HASH);
	        cf = cfb.buildCouchbaseConnection(uris, bucketName, bucketPassword);
	     		
	        client = new CouchbaseClient(cf); 
			client.addObserver(new ConnectionObserver() {
				public void connectionLost(SocketAddress arg0) {
					log.debug("Couchbase Connection Lost");
				}
				
				public void connectionEstablished(SocketAddress arg0, int arg1) {
					log.debug("Couchbase Connection Established");
				}
			});
	    } catch (Exception e) {
	    	throw(new KeyValueStoreClientException(e));
	    }					
		return client;
	}
	
	public boolean establish() throws KeyValueStoreClientException {
		if (_client != null) {
			shutdown();
		}
		if (_fallbackClient != null) {
			shutdown();
		}
	    try {
	    	_client = connectToCouchbase(_serverString, _bucketName, _bucketPassword);
	    	_fallbackClient = connectToCouchbase(_fallbackServerString, _fallbackBucketName, _fallbackBucketPassword);
	    } catch (Exception e) {
	    	throw(new KeyValueStoreClientException(e));
	    }					

		return true;
	}

	public boolean shutdown() throws KeyValueStoreClientException {
		if (_client != null) {
			_client.shutdown();
			_client = null;
		}
		return true;
	}

	public boolean isAlive() {
		return _client != null;
	}

	public byte[] get(String key) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		byte[] raw = null;
		try {
			Future<byte[]> f = _client.asyncGet(key, _transcoder);
			raw = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		
		try {
			if(raw == null && _fallbackClient != null){
				Future<byte[]> f = _fallbackClient.asyncGet(key, _transcoder);
				raw = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
			}
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}

		return raw;
	}

	public boolean set(String key, byte[] raw) throws KeyValueStoreClientException {
		return this.set(key, raw, FOREVER);
	}

	public boolean set(String key, byte[] raw, int exp) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		boolean result;
		try {
			Future<Boolean> f = _client.set(key, exp, raw, _transcoder);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}

	public boolean add(String key, byte[] raw) throws KeyValueStoreClientException {
		return this.add(key, raw, FOREVER);
	}

	public boolean add(String key, byte[] raw, int exp) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		boolean result;
		try {
			Future<Boolean> f = _client.add(key, exp, raw, _transcoder);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}

	public boolean delete(String key) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		boolean result;
		try {
			Future<Boolean> f = _client.delete(key);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}
}

