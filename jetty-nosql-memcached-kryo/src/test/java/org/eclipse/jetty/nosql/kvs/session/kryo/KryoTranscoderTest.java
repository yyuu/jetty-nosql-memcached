package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractTranscoderTest;
import org.eclipse.jetty.nosql.kvs.session.kryo.KryoTranscoder;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;

public class KryoTranscoderTest extends AbstractTranscoderTest {
	@Override
	public ISerializationTranscoder createTranscoder() {
		return new KryoTranscoder();
	}
}
