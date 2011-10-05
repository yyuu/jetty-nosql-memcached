package org.eclipse.jetty.nosql.session.serializable;

import org.eclipse.jetty.nosql.session.AbstractTranscoderTest;
import org.eclipse.jetty.nosql.session.ISerializationTranscoder;
import org.eclipse.jetty.nosql.session.serializable.SerializableTranscoder;

public class SerializableTranscoderTest extends AbstractTranscoderTest {
	@Override
	public ISerializationTranscoder createTranscoder() {
		return new SerializableTranscoder();
	}
}
