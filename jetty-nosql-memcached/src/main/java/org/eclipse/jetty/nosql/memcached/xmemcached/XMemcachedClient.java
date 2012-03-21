package org.eclipse.jetty.nosql.memcached.xmemcached;

import java.io.IOException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.transcoders.Transcoder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;
import org.eclipse.jetty.nosql.memcached.AbstractMemcachedClient;

public class XMemcachedClient extends AbstractMemcachedClient {
	private static final int FOREVER = 0;
	private XMemcachedClientBuilder _builder = null;
	private MemcachedClient _connection = null;
	private Transcoder<byte[]> _transcoder = null;

	public XMemcachedClient() {
		this("127.0.0.1:11211");
	}

	public XMemcachedClient(String serverString) {
		super(serverString);
		this._transcoder = new NullTranscoder();
	}

	public boolean establish() throws KeyValueStoreClientException {
		if (_connection != null) {
			if (!_connection.isShutdown()) {
				return true;
			} else {
				shutdown();
			}
		}
		
		this._builder = getClientBuilder(_serverString);
		try {
			this._connection = _builder.build();
		} catch (IOException error) {
			throw(new KeyValueStoreClientException(error));
		}
		return true;
	}

	protected XMemcachedClientBuilder getClientBuilder(String serverString) {
		XMemcachedClientBuilder builder =  new XMemcachedClientBuilder(AddrUtil.getAddresses(serverString));
		builder.setTranscoder(_transcoder);
		return builder;
	}

	public boolean shutdown() throws KeyValueStoreClientException {
		if (_connection != null) {
			try {
				_connection.shutdown();
			} catch (IOException error) {
				throw(new KeyValueStoreClientException(error));
			} finally {
				_connection = null;
			}
		}
		return true;
	}

	public boolean isAlive() {
		return this._connection != null && !this._connection.isShutdown();
	}

	public byte[] get(String key) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		byte[] raw = null;
		try {
			raw = _connection.get(key);
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
		boolean result = false;
		try {
			result = _connection.set(key, exp, raw);
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
		boolean result = false;
		try {
			result = _connection.add(key, exp, raw);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}

	public boolean delete(String key) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		boolean result = false;
		try {
			result = _connection.delete(key);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}
}