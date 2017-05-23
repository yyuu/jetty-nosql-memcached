package org.eclipse.jetty.nosql.kvs.session.serializable;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;

import java.util.Map;

public class SerializableSession extends AbstractSerializableSession {
    private static final long serialVersionUID = 8840014279617118388L;

    public SerializableSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs) {
        super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs);
    }

    public SerializableSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs, Map<String, Object> attributes) {
        super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs, attributes);
    }

}