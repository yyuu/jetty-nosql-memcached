package org.eclipse.jetty.nosql.memcached.xmemcached;

import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.command.BinaryCommandFactory;

/**
 * Created by IntelliJ IDEA.
 * User: yyuu
 * Date: 12/03/11
 * Time: 0:08
 * To change this template use File | Settings | File Templates.
 */
public class BinaryXMemcachedClient extends XMemcachedClient {
	public BinaryXMemcachedClient() {
		super();
	}

	public BinaryXMemcachedClient(String serverString) {
		super(serverString);
	}

	@Override
	protected XMemcachedClientBuilder getClientBuilder(String serverString) {
		XMemcachedClientBuilder builder = super.getClientBuilder(serverString);
		builder.setCommandFactory(new BinaryCommandFactory());
		return builder;
	}
}
