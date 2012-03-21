package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractTranscoderTest;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableTranscoder;

public class SerializableTranscoderTest extends AbstractTranscoderTest {
	@Override
	public ISerializationTranscoder createTranscoder() {
		return new SerializableTranscoder();
	}
}
