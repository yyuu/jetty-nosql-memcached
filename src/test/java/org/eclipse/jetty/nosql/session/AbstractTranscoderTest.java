package org.eclipse.jetty.nosql.session;

import java.io.Serializable;
import java.util.Arrays;
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
		assertEquals(f1, f2, 0.1);
	}

	public void testDouble() throws Exception {
		double d1 = 3.1415926;
		byte[] raw = transcoder.encode(d1);
		assertNotNull(raw);
		double d2 = transcoder.decode(raw,  Double.class).doubleValue();
		assertNotNull(d2);
		assertEquals(d1, d2, 0.1);
	}

	public void testString() throws Exception {
		String str1 = "foo";
		byte[] raw = transcoder.encode(str1);
		assertNotNull(raw);
		String str2 = transcoder.decode(raw, String.class);
		assertNotNull(str2);
		assertEquals(str1, str2);
	}

	public void testByteArray() throws Exception {
		byte[] bs1 = {11, 22, 33, 44, 55};
		byte[] raw = transcoder.encode(bs1);
		assertNotNull(raw);
		byte[] bs2 = transcoder.decode(raw, byte[].class);
		assertNotNull(bs2);
		assertTrue(Arrays.equals(bs1, bs2));
	}

	public void testStringArray() throws Exception {
		String[] ss1 = {"foo", "bar", "baz"};
		byte[] raw = transcoder.encode(ss1);
		assertNotNull(raw);
		String[] ss2 = transcoder.decode(raw, String[].class);
		assertNotNull(ss2);
		assertTrue(Arrays.equals(ss1, ss2));
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
		Pojo pojo1 = new Pojo("xxx", 1111, 2.2222);
		byte[] raw = transcoder.encode(pojo1);
		assertNotNull(raw);
		Pojo pojo2 = transcoder.decode(raw, Pojo.class);
		assertNotNull(pojo2);
		assertEquals(pojo1.getFoo(), pojo2.getFoo());
		assertEquals(pojo1.getBar(), pojo2.getBar());
		assertEquals(pojo1.getBaz(), pojo2.getBaz(), 0.1);

		Pojo pojo3 = new Pojo("zzz", 3333, 4.4444);
		assertFalse(pojo1.getFoo().equals(pojo3.getFoo()));
		assertFalse(pojo1.getBar() == pojo3.getBar());
		assertFalse(pojo1.getBaz() == pojo3.getBaz());
	}
}

class Pojo implements Serializable {
	private static final long serialVersionUID = 1624917957114907302L;
	public String foo;
	protected int bar;
	private double baz;
	public Pojo() {
		foo = "foo value";
		bar = 2222;
		baz = 3.333;
	}
	public Pojo(String f1, int b1, double b2) {
		foo = f1;
		bar = b1;
		baz = b2;
	}
	public String getFoo() {
		return foo;
	}
	public int getBar() {
		return bar;
	}
	public double getBaz() {
		return baz;
	}
}