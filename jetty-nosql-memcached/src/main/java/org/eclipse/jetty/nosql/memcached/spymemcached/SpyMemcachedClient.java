package org.eclipse.jetty.nosql.memcached.spymemcached;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;
import org.eclipse.jetty.nosql.memcached.AbstractMemcachedClient;

public class SpyMemcachedClient extends AbstractMemcachedClient {
	private static final int FOREVER = 0;
	private MemcachedClient _client = null;
	private Transcoder<byte[]> _transcoder = null;

	public SpyMemcachedClient() {
		this("127.0.0.1:11211");
	}

	public SpyMemcachedClient(String serverString) {
		super(serverString);
		this._transcoder = new NullTranscoder();
	}

	public boolean establish() throws KeyValueStoreClientException {
		if (_client != null) {
			shutdown();
		}
		try {
			ConnectionFactory cf = getConnectionFactory();
			if (cf == null) {
				this._client = new MemcachedClient(AddrUtil.getAddresses(_serverString));
			} else {
				this._client = new MemcachedClient(cf, AddrUtil.getAddresses(_serverString));
			}
		} catch (IOException error) {
			throw(new KeyValueStoreClientException(error));
		}
		return true;
	}

	protected ConnectionFactoryBuilder getConnectionFactoryBuilder() {
		return new ConnectionFactoryBuilder();
	}

	protected ConnectionFactory getConnectionFactory() {
		ConnectionFactoryBuilder factoryBuilder = getConnectionFactoryBuilder();
		return factoryBuilder.build();
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
