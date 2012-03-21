package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractTranscoderTest;
import org.eclipse.jetty.nosql.kvs.session.xstream.XStreamTranscoder;
import org.eclipse.jetty.nosql.kvs.session.ISerializationTranscoder;

public class XStreamTranscoderTest extends AbstractTranscoderTest {
	@Override
	public ISerializationTranscoder createTranscoder() {
		return new XStreamTranscoder();
	}
}
