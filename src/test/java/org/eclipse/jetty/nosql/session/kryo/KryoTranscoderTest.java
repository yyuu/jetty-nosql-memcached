package org.eclipse.jetty.nosql.session.kryo;

import org.eclipse.jetty.nosql.session.AbstractTranscoderTest;
import org.eclipse.jetty.nosql.session.ISerializationTranscoder;

public class KryoTranscoderTest extends AbstractTranscoderTest {
	@Override
	public ISerializationTranscoder createTranscoder() {
		return new KryoTranscoder();
	}
}
