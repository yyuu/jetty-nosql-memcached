package org.eclipse.jetty.nosql.memcached.spymemcached;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;

public class SpyMemcachedClient implements KeyValueStoreClient {
	private static final int FOREVER = 0;
	private String _serverString = "127.0.0.1:11211";
	private MemcachedClient _connection = null;
	private Transcoder<byte[]> _transcoder = null;
	private int _timeoutInMs = 1000;

	public SpyMemcachedClient() {
		this("127.0.0.1:11211");
	}

	public SpyMemcachedClient(String serverString) {
		this._serverString = serverString;
		this._transcoder = new NullTranscoder();
	}

	@Override
	public boolean establish() throws KeyValueStoreClientException {
		if (_connection != null) {
			if (_connection.isAlive()) {
				return true;
			} else {
				shutdown();
			}
		}
		try {
			this._connection = new MemcachedClient(AddrUtil.getAddresses(_serverString));
		} catch (IOException error) {
			throw(new KeyValueStoreClientException(error));
		}
		return true;
	}

	@Override
	public boolean shutdown() throws KeyValueStoreClientException {
		if (_connection != null) {
			_connection.shutdown();
			_connection = null;
		}
		return true;
	}

	@Override
	public boolean isAlive() {
		return _connection != null && _connection.isAlive();
	}

	@Override
	public byte[] get(String key) throws KeyValueStoreClientException {
		byte[] raw = null;
		try {
			Future<byte[]> f = _connection.asyncGet(key, _transcoder);
			raw = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return raw;
	}

	@Override
	public boolean set(String key, byte[] raw) throws KeyValueStoreClientException {
		return this.set(key, raw, FOREVER);
	}

	@Override
	public boolean set(String key, byte[] raw, int exp) throws KeyValueStoreClientException {
		boolean result = false;
		try {
			Future<Boolean> f = _connection.set(key, exp, raw, _transcoder);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}

	@Override
	public boolean add(String key, byte[] raw) throws KeyValueStoreClientException {
		return this.add(key, raw, FOREVER);
	}

	@Override
	public boolean add(String key, byte[] raw, int exp) throws KeyValueStoreClientException {
		boolean result = false;
		try {
			Future<Boolean> f = _connection.add(key, exp, raw, _transcoder);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}

	@Override
	public boolean delete(String key) throws KeyValueStoreClientException {
		boolean result = false;
		try {
			Future<Boolean> f = _connection.delete(key);
			result = f.get(_timeoutInMs, TimeUnit.MILLISECONDS);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}

	public String getServerString() {
		return _serverString;
	}

	public void setServerString(String _serverString) {
		this._serverString = _serverString;
	}

	public int getTimeoutInMs() {
		return _timeoutInMs;
	}

	public void setTimeoutInMs(int _timeoutInMs) {
		this._timeoutInMs = _timeoutInMs;
	}
}
