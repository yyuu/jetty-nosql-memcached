package org.eclipse.jetty.nosql.kvs.session;

import org.eclipse.jetty.server.session.SessionData;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;


public abstract class AbstractSerializableSession extends SessionData {
    private static final long serialVersionUID = -503405419124071511L;
	public long _version = 0;

	public AbstractSerializableSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs) {
		super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs);
	}
	public AbstractSerializableSession(String id, String cpath, String vhost, long created, long accessed, long lastAccessed, long maxInactiveMs, Map<String, Object> attributes) {
		super(id, cpath, vhost, created, accessed, lastAccessed, maxInactiveMs, attributes);
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public long getAccessed() {
		return _accessed;
	}

	public void setAccessed(long accessed) {
		this._accessed = accessed;
	}

	public synchronized Map<String, Object> getAttributeMap() {
		return Collections.unmodifiableMap(_attributes);
	}

	public synchronized void setAttributeMap(Map<String, Object> attributes) {
		this._attributes = attributes;
	}

	public synchronized Object getAttribute(String key) {
		return _attributes.get(key);
	}

	public synchronized void removeAttribute(String key) {
		_attributes.remove(key);
	}

	public synchronized Enumeration<String> getAttributeNames() {
		return Collections.enumeration(this._attributes.keySet());
	}

	@Override
	public String toString() {
		return "{id:" + getId() +
				", created:" + getCreated() +
				", accessed:" + getAccessed() +
				", attributes:" + getAttributeMap() +
		"}";
	}

	public long getVersion() {
		return _version;
	}

	public void setVersion(long version) {
		this._version = version;
	}

	public boolean equals(AbstractSerializableSession other) {
		System.out.println("this=" + this + ", other=" + other);
		boolean result = other != null;
		result = result && this.getCreated() == other.getCreated();
		result = result && this.getAccessed() == other.getAccessed();
		result = result && this.getVersion() == other.getVersion();
		if (result) {
			if (this.getId() == null) {
				result = result && other.getId() == null;
			} else {
				result = result && this.getId().equals(other.getId());
			}
		}
		if (result) {
			if (this.getVhost() == null) {
				result = result && other.getVhost() == null;
			} else {
				result = result && this.getVhost().equals(other.getVhost());
			}
		}
		if (result) {
			if (this.getContextPath() == null) {
				result = result && other.getContextPath() == null;
			} else {
				result = result && this.getContextPath().equals(other.getContextPath());
			}
		}
		if (result) {
			if (this.getAttributeMap() == null) {
				result = result && other.getAllAttributes() == null;
			} else {
				result = result && this.getAttributeMap().equals(other.getAllAttributes());
			}
		}
		return result;
	}
}
