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
	private MemcachedClient _client = null;
	private Transcoder<byte[]> _transcoder = null;

	public XMemcachedClient() {
		this("127.0.0.1:11211");
	}

	public XMemcachedClient(String serverString) {
		super(serverString);
		this._transcoder = new NullTranscoder();
	}

	public boolean establish() throws KeyValueStoreClientException {
		if (_client != null) {
			if (!_client.isShutdown()) {
				return true;
			} else {
				shutdown();
			}
		}
		
		this._builder = getClientBuilder(_serverString);
		try {
			this._client = _builder.build();
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
		if (_client != null) {
			try {
				_client.shutdown();
			} catch (IOException error) {
				throw(new KeyValueStoreClientException(error));
			} finally {
				_client = null;
			}
		}
		return true;
	}

	public boolean isAlive() {
		return this._client != null && !this._client.isShutdown();
	}

	public byte[] get(String key) throws KeyValueStoreClientException {
		if (!isAlive()) {
			throw(new KeyValueStoreClientException(new IllegalStateException("client not established")));
		}
		byte[] raw = null;
		try {
			raw = _client.get(key);
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
			result = _client.set(key, exp, raw);
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
			result = _client.add(key, exp, raw);
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
			result = _client.delete(key);
		} catch (Exception error) {
			throw(new KeyValueStoreClientException(error));
		}
		return result;
	}
}