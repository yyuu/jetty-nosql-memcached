package org.eclipse.jetty.nosql.session;

public interface ISerializationTranscoder {
	/**
	 * serialize an object to byte array
	 * @param object to serialize
	 * @return serialized data
	 * @throws Exception
	 */
	public byte[] encode(Object obj) throws Exception;

	/**
	 * deserialize object(s) from byte array
	 * @param serialized data
	 * @param type of serialized data
	 * @return deserialized object(s)
	 * @throws Exception
	 */
	public <T> T decode(byte[] raw, Class<T> klass) throws Exception;
}
