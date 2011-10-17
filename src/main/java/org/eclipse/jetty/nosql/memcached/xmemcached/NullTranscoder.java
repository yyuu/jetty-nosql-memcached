package org.eclipse.jetty.nosql.memcached.xmemcached;

import net.rubyeye.xmemcached.transcoders.CachedData;
import net.rubyeye.xmemcached.transcoders.PrimitiveTypeTranscoder;

public class NullTranscoder extends PrimitiveTypeTranscoder<byte[]> {
	private static final int flags = 0;

	public CachedData encode(byte[] bs) {
		return new CachedData(flags, bs);
	}

	public byte[] decode(CachedData d) {
		if (d == null) {
			return null;
		}
		return d.getData();
	}
}
