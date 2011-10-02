package org.eclipse.jetty.nosql.memcached;

import net.spy.memcached.CachedData;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;

public class ByteArrayTranscoder extends SpyObject implements Transcoder<byte[]> {
	private static final int flags = 0;

	public boolean asyncDecode(CachedData d) {
		return false;
	}

	public CachedData encode(byte[] bs) {
		return new CachedData(flags, bs, getMaxSize());
	}

	public byte[] decode(CachedData d) {
		if (flags == d.getFlags()) {
			return d.getData();
		} else {
			getLogger().error("Unexpected flags for long:  "
				+ d.getFlags() + " wanted " + flags);
			return null;
		}
	}

	public int getMaxSize() {
		return CachedData.MAX_SIZE;
	}
}
