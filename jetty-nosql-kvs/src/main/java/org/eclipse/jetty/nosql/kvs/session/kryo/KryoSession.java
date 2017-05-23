package org.eclipse.jetty.nosql.kvs.session.kryo;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;

import java.util.Map;

public class KryoSession extends AbstractSerializableSession {
    private static final long serialVersionUID = -5831466917244198745L;

    // TODO: Kryo needs no args constructor?
    public KryoSession(){
        super("", "*", "*", 0, 0, 0, 0);
        long now = System.currentTimeMillis();
        setCreated(now);
        setAccessed(now);
        setLastAccessed(now);
    }

    public KryoSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs) {
        super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs);
    }

    public KryoSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs, Map<String, Object> attributes) {
        super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs, attributes);
    }

}
