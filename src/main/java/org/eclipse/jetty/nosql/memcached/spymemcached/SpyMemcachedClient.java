package org.eclipse.jetty.nosql.memcached.spymemcached;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;

public class SpyMemcachedClient extends AbstractKeyValueStoreClient {
	private static final int FOREVER = 0;
	private MemcachedClient _connection = null;
	private Transcoder<byte[]> _transcoder = null;

	public SpyMemcachedClient() {
		this("127.0.0.1:11211");
	}

	public SpyMemcachedClient(String serverString) {
		super(serverString);
		this._transcoder = new NullTranscoder();
	}
	
	@Override
	public void setServerString(String _serverString) {
		StringBuilder serverString = new StringBuilder();
		for (String s: _serverString.trim().split("[\\s,]+")) {
			serverString.append((0 < serverString.length() ? " " : "") + s);
		}

		this._serverString = serverString.toString();
	}

	public boolean establish() throws KeyValueStoreClientException {
		if (_connection != null) {
			if (_connection.isAlive()) {
				return true;
			} else {
				shutdown();
			}
		}
		try {
			ConnectionFactory cf = getConnectionFactory();
			if (cf == null) {
				this._connection = new MemcachedClient(AddrUtil.getAddresses(_serverString));
			} else {
				this._connection = new MemcachedClient(cf, AddrUtil.getAddresses(_serverString));
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
		if (_connection != null) {
			_connection.shutdown();
			_connection = null;
		}
		return true;
	}

	public boolean isAlive() {
		return _connection != null && _connection.isAlive();
	}

	public byte[] get(String key) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		byte[] raw;
		try {
			Future<byte[]> f = _connection.asyncGet(key, _transcoder);
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
			Future<Boolean> f = _connection.set(key, exp, raw, _transcoder);
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
			Future<Boolean> f = _connection.add(key, exp, raw, _transcoder);
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
			Future<Boolean> f = _connection.delete(key);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}
}
