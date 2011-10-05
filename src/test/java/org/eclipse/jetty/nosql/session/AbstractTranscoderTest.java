package org.eclipse.jetty.nosql.session;

import java.util.HashMap;
import junit.framework.TestCase;

public abstract class AbstractTranscoderTest extends TestCase {
	protected ISerializationTranscoder transcoder = null;
	
	public void setUp() throws Exception {
		transcoder = createTranscoder();
	}
	
	public abstract ISerializationTranscoder createTranscoder();

	public void testInt() throws Exception {
		int int1 = 31415926;
		byte[] raw = transcoder.encode(int1);
		assertNotNull(raw);
		int int2 = transcoder.decode(raw, Integer.class).intValue();
		assertNotNull(int2);
		assertEquals(int1, int2);
	}

	public void testFloat() throws Exception {
		float f1 = 3.1415926F;
		byte[] raw = transcoder.encode(f1);
		assertNotNull(raw);
		float f2 = transcoder.decode(raw, Float.class).floatValue();
		assertNotNull(f2);
		assertEquals(f1, f2);
	}

	public void testString() throws Exception {
		String str1 = "foo";
		byte[] raw = transcoder.encode(str1);
		assertNotNull(raw);
		String str2 = transcoder.decode(raw, String.class);
		assertNotNull(str2);
		assertEquals(str1, str2);
	}

	public void testArray() throws Exception {
		// TODO:
	}

	public void testMap() throws Exception {
		HashMap<String, String> map1 = new HashMap<String, String>();
		map1.put("foo", "foo value");
		map1.put("bar", "bar value");
		map1.put("baz", "baz value");
		byte[] raw = transcoder.encode(map1);
		assertNotNull(raw);
		@SuppressWarnings("unchecked")
		HashMap<String, String> map2 = transcoder.decode(raw, HashMap.class);
		assertNotNull(map2);
		assertEquals(map1, map2);
	}

	public void testPojo() throws Exception {
		// TODO:
	}
}
