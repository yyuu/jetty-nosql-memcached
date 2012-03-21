package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;
import org.eclipse.jetty.nosql.memcached.hashmap.HashMapClient;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: yyuu
 * Date: 12/02/20
 * Time: 12:32
 * To change this template use File | Settings | File Templates.
 */
public class HashMapClientTest {
	AbstractMemcachedClient client = new HashMapClient();

	public void setUp() {
		try {
			client.establish();
		} catch (KeyValueStoreClientException e) {
			// nop
		}
	}

	public void tearDown() {
		try {
			client.shutdown();
		} catch (KeyValueStoreClientException e) {
			// nop
		}
	}

	@Test
	public void testDefaultPrepareServerString() throws Exception {
		assertEquals("foo", client.prepareServerString("foo"));
		assertEquals("bar:12345", client.prepareServerString("  bar:12345"));
		assertEquals("foo bar:12345", client.prepareServerString("  foo   bar:12345   "));
		assertEquals("foo bar:12345 baz:678", client.prepareServerString("  foo   bar:12345        baz:678"));
	}

	@Test
	public void testCommaSeparatedPrepareServerString() throws Exception {
		assertEquals("foo bar:12345", client.prepareServerString("foo,bar:12345"));
		assertEquals("foo bar:12345", client.prepareServerString("  foo,  bar:12345   "));
		assertEquals("foo bar:12345", client.prepareServerString("  foo , bar:12345 , "));
		assertEquals("foo bar:12345 baz:678", client.prepareServerString("  foo,  bar:12345,       baz:678"));
	}
}
