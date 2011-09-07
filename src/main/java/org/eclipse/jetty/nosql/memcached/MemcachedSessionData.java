package org.eclipse.jetty.nosql.memcached;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.eclipse.jetty.server.session.AbstractSession;

public class MemcachedSessionData implements Serializable {
	private static final long serialVersionUID = -3998063962105675446L;
	public String _id = "";
	public long _created = -1;
	public long _accessed = -1;
	public boolean _valid = true;
	public long _invalidated = -1;
	public long _version = 0;
	public Map<String, Object> _attributes = new HashMap<String, Object>();
	public String _contextPath = "*";
	
	public MemcachedSessionData() {
		this._created = System.currentTimeMillis();
		this._accessed = _created;
	}

	public MemcachedSessionData(String sessionId) {
		this._id = sessionId;
		this._created = System.currentTimeMillis();
		this._accessed = _created;
	}
	
	public MemcachedSessionData(String sessionId, long created) {
		this._id = sessionId;
		this._created = created;
		this._accessed = System.currentTimeMillis();
	}

	public MemcachedSessionData(AbstractSession session) {
		this._id = session.getId();
		this._created = session.getCreationTime();
		this._accessed = session.getAccessed();
		setValid(session.isValid());
		for (Enumeration<String> e=session.getAttributeNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			this._attributes.put(key, session.getAttribute(key));
		}
	}

	public String getId() {
		return _id;
	}

	public void setId(String id) {
		if (id == null) {
			id = "";
		}
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
		return "{id:" + getId() +
				", created:" + getCreationTime() +
				", accessed:" + getAccessed() +
				", attributes:" + getAttributeMap() +
				", valid:" + isValid() +
				", invalidated:" + getInvalidated() +
		"}";
	}

	public boolean isValid() {
		return _valid;
	}

	public void setValid(boolean valid) {
		setValid(valid, System.currentTimeMillis());
	}
	
	public void setValid(boolean valid, long invalidated) {
		this._valid = valid;
		if (!valid) {
			setInvalidated(invalidated);
		}
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
	
	public String getContextPath() {
		return _contextPath;
	}
	
	public void setContextPath(String contextPath) {
		this._contextPath = contextPath;
	}

	public static byte[] pack(MemcachedSessionData data) throws Exception {
		if (data == null) {
			return null;
		}
		byte[] raw = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(data);
			raw = baos.toByteArray();
		} catch (Exception error) {
			throw(error);
		}
		return raw;
	}

	public static MemcachedSessionData unpack(byte[] raw) throws Exception {
		if (raw == null) {
			return null;
		}
		MemcachedSessionData data = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(raw);
			ObjectInputStream ois = new ObjectInputStream(bais);
			data = (MemcachedSessionData) ois.readObject();
		} catch (Exception error) {
			throw(error);
		}
		return data;
	}
}