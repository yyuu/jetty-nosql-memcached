package org.eclipse.jetty.nosql.kvs.session;

import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractSerializableSession implements ISerializableSession, Serializable {
	private static final long serialVersionUID = -8960779543485104697L;
	public String _id = "";
	public long _created = -1;
	public long _accessed = -1;
	public long _invalidated = -1;
	public long _version = 0;
	public Map<String, Object> _attributes = new HashMap<String, Object>();
	public String _domain = "*";
	public String _path = "*";

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		this._id = id;
	}

	public long getCreationTime() {
		return _created;
	}

	public void setCreationTime(long created) {
		this._created = created;
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

	public synchronized void setAttribute(String key, Object obj) {
		_attributes.put(key, obj);
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
				", created:" + getCreationTime() +
				", accessed:" + getAccessed() +
				", attributes:" + getAttributeMap() +
				", invalidated:" + getInvalidated() +
		"}";
	}

	public boolean isValid() {
		return _invalidated < 0;
	}

	public void setValid(boolean valid) {
		if (valid) {
			this._invalidated = -1L;
		} else {
			this._invalidated = System.currentTimeMillis();
		}
	}

	public long getInvalidated() {
		return _invalidated;
	}

	public long getVersion() {
		return _version;
	}

	public void setVersion(long version) {
		this._version = version;
	}

	public String getDomain() {
		return _domain;
	}

	public void setDomain(String domain) {
		this._domain = domain;
	}

	public String getPath() {
		return _path;
	}

	public void setPath(String path) {
		this._path = path;
	}

	public boolean equals(ISerializableSession other) {
		System.out.println("this=" + this + ", other=" + other);
		boolean result = other != null;
		result = result && this.getCreationTime() == other.getCreationTime();
		result = result && this.getAccessed() == other.getAccessed();
		result = result && this.getInvalidated() == other.getInvalidated();
		result = result && this.getVersion() == other.getVersion();
		if (result) {
			if (this.getId() == null) {
				result = result && other.getId() == null;
			} else {
				result = result && this.getId().equals(other.getId());
			}
		}
		if (result) {
			if (this.getDomain() == null) {
				result = result && other.getDomain() == null;
			} else {
				result = result && this.getDomain().equals(other.getDomain());
			}
		}
		if (result) {
			if (this.getPath() == null) {
				result = result && other.getPath() == null;
			} else {
				result = result && this.getPath().equals(other.getPath());
			}
		}
		if (result) {
			if (this.getAttributeMap() == null) {
				result = result && other.getAttributeMap() == null;
			} else {
				result = result && this.getAttributeMap().equals(other.getAttributeMap());
			}
		}
		return result;
	}
}
