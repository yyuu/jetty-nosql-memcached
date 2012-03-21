package org.eclipse.jetty.nosql.memcached.spymemcached;

import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;

public class NullTranscoder extends SpyObject implements Transcoder<byte[]> {
// Kyoty Tycoon accepts only flags==0 on storing data.
	private static final int flags = 0;

	public boolean asyncDecode(CachedData d) {
		return false;
	}

	public CachedData encode(byte[] bs) {
		return new CachedData(flags, bs, getMaxSize());
	}

	public byte[] decode(CachedData d) {
		if (d == null) {
			return null;
		}
		return d.getData();
	}

	public int getMaxSize() {
		return CachedData.MAX_SIZE;
	}
}
