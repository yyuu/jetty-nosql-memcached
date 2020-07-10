package org.eclipse.jetty.nosql.kvs.session.xstream;

import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;

import java.util.Map;

public class XStreamSession extends AbstractSerializableSession {
    private static final long serialVersionUID = -5311083892860467664L;

    public XStreamSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs) {
        super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs);
    }

    public XStreamSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs, Map<String, Object> attributes) {
        super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs, attributes);
    }

}
