package org.eclipse.jetty.nosql.kvs.session;

public interface ISerializationTranscoder {
//	public byte[] encode(boolean b) throws TranscoderException;
//
//	public byte[] encode(char c) throws TranscoderException;
//
//	public byte[] encode(byte b) throws TranscoderException;
//
//	public byte[] encode(short s) throws TranscoderException;
//
//	public byte[] encode(int i) throws TranscoderException;
//
//	public byte[] encode(long l) throws TranscoderException;
//
//	public byte[] encode(float f) throws TranscoderException;
//
//	public byte[] encode(double d) throws TranscoderException;

	/**
	 * serialize an object to byte array
	 * @param object to serialize
	 * @return serialized data
	 * @throws Exception
	 */
	public byte[] encode(Object obj) throws TranscoderException;

	/**
	 * deserialize object(s) from byte array
	 * @param serialized data
	 * @param type of serialized data
	 * @return deserialized object(s)
	 * @throws Exception
	 */
	public <T> T decode(byte[] raw, Class<T> klass) throws TranscoderException;
}
