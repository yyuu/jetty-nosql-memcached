package org.eclipse.jetty.nosql.memcached;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.jetty.nosql.NoSqlSession;
import org.eclipse.jetty.server.session.AbstractSession;

public class MemcachedSessionData implements Serializable {
	private static final long serialVersionUID = -3998063962105675446L;
	public String _id = "_id";
	public long _created = -1;
	public long _cookieSet = -1;
	public long _accessed = -1;
	public long _lastAccessed = -1;
	public String _workerName = "_workerName";
	public boolean _invalid = false;
	public long _invalidated = -1;
	public long _version = 0;
	public Map<String, Object> _attributes = new HashMap<String, Object>();

	public MemcachedSessionData() {
		this._created = System.currentTimeMillis();
		this._accessed = _created;
	}

	public MemcachedSessionData(String sessionId) {
		this._id = sessionId;
		this._created = System.currentTimeMillis();
		this._accessed = _created;
	}

	public MemcachedSessionData(String sessionId, Map<String, Object> attributes) {
		this._id = sessionId;
		this._created = System.currentTimeMillis();
		this._accessed = _created;
		this._attributes = attributes;
	}

	public MemcachedSessionData(AbstractSession session) {
		this._id = session.getId();
		this._created = session.getCreationTime();
		this._accessed = session.getAccessed();
		for (Enumeration<String> e = session.getAttributeNames(); e
				.hasMoreElements();) {
			String key = e.nextElement();
			this._attributes.put(key, session.getAttribute(key));
		}
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		if (id == null)
			id = "";
		this._id = id;
	}

	public long getCreationTime() {
		return _created;
	}

	public void setCreationTime(long created) {
		this._created = created;
	}

	public long getCookieSetTime() {
		return _cookieSet;
	}

	public void setCookieSetTime(long cookieSet) {
		this._cookieSet = cookieSet;
	}

	public long getAccessedTime() {
		return _accessed;
	}

	public void setAccessedTime(long accessed) {
		this._accessed = accessed;
	}

	public long getLastAccessedTime() {
		return _lastAccessed;
	}

	public void setLastAccessedTime(long lastAccessed) {
		this._lastAccessed = lastAccessed;
	}

	public String getWorkerName() {
		return _workerName;
	}

	public void setWorkerName(String workerName) {
		if (workerName == null)
			workerName = null;
		this._workerName = workerName;
	}

	public synchronized Map<String, Object> getAttributeMap() {
		return _attributes;
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
		return "{id:" + _id + ", lastNode:" + getWorkerName() + ", created:"
				+ getCreationTime() + ", accessed:" + getAccessedTime()
				+ ", lastAccessed:" + getLastAccessedTime() + ", attributes: "
				+ getAttributeMap() + "}";
	}

	public boolean isInvalid() {
		return _invalid;
	}

	public void setInvalid(boolean invalid) {
		this._invalid = invalid;
	}

	public long getInvalidated() {
		return _invalidated;
	}

	public void setInvalidated(long invalidated) {
		this._invalidated = invalidated;
	}

	public long getVersion() {
		return _version;
	}

	public void setVersion(long version) {
		this._version = version;
	}

	public static byte[] pack(MemcachedSessionData obj) {
		if (obj == null) {
			return null;
		}
		byte[] raw = null;
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bout);
			out.writeObject(obj);
			raw = bout.toByteArray();
		} catch (IOException error) {
			//
		}
		return raw;
	}

	public static MemcachedSessionData unpack(byte[] raw) {
		if (raw == null) {
			return null;
		}
		MemcachedSessionData obj = null;
		try {
			ByteArrayInputStream bin = new ByteArrayInputStream(raw);
			ObjectInputStream in = new ObjectInputStream(bin);
			obj = (MemcachedSessionData) in.readObject();
		} catch (IOException error) {
			// TODO: log messages
		} catch (ClassNotFoundException error) {
			// TODO: log messages
		}
		return obj;
	}
}