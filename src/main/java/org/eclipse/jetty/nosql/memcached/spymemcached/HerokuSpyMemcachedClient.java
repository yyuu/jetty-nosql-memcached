package org.eclipse.jetty.nosql.memcached.spymemcached;

import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;

public class HerokuSpyMemcachedClient extends BinarySpyMemcachedClient {
    /**
     * special client for heroku (binary protocol + SASL)
     *
     * TODO: need more general way to support platforms other than heroku
     */
	private String _username = "";
	private String _password = "";
    
	public HerokuSpyMemcachedClient() {
		this("127.0.0.1:11211");
	}

	public HerokuSpyMemcachedClient(String _) {
        super(_);

        String servers = System.getenv("MEMCACHE_SERVERS");
        String username = System.getenv("MEMCACHE_USERNAME");
        String password = System.getenv("MEMCACHE_PASSWORD");
        if (servers == null || username == null || password == null) {
            throw new RuntimeException("not enough environment variables set for heroku environment");
        }

        StringBuilder sb = new StringBuilder();
        for (String s: servers.split("\\s+")) {
            int finalColon = s.lastIndexOf(":");
            String server = 0 < finalColon ? s : (s + ":" + 11211);
            sb.append(server + " ");
        }
        setServerString(sb.toString().trim());

		this._username = username;
		this._password = password;
	}

	@Override
	protected ConnectionFactoryBuilder getConnectionFactoryBuilder() {
		ConnectionFactoryBuilder factoryBuilder = super.getConnectionFactoryBuilder();
		AuthDescriptor ad = new AuthDescriptor(new String[] {"PLAIN"}, new PlainCallbackHandler(_username, _password));
		factoryBuilder.setAuthDescriptor(ad);
		return factoryBuilder;
	}
}