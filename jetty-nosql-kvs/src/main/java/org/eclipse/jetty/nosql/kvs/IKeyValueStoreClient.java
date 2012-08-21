package org.eclipse.jetty.nosql.kvs;

public interface IKeyValueStoreClient {
	public boolean establish() throws KeyValueStoreClientException;

	public boolean shutdown() throws KeyValueStoreClientException;

	public boolean isAlive();

	public byte[] get(String key) throws KeyValueStoreClientException;

// "set" means "store this data".
	public boolean set(String key, byte[] raw) throws KeyValueStoreClientException;

	public boolean set(String key, byte[] raw, int exp) throws KeyValueStoreClientException;

// "add" means "store this data, but only if the server *doesn't* already
// hold data for this key".
	public boolean add(String key, byte[] raw) throws KeyValueStoreClientException;

	public boolean add(String key, byte[] raw, int exp) throws KeyValueStoreClientException;

	public boolean delete(String key) throws KeyValueStoreClientException;
}
