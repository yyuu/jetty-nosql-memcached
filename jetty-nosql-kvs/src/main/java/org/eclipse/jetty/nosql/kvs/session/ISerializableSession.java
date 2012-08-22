package org.eclipse.jetty.nosql.kvs.session;

import java.util.Enumeration;
import java.util.Map;

public interface ISerializableSession {
	/**
	 * 
	 * @return string form of id
	 */
	public String getId();
	/**
	 * 
	 * @param id string form of id
	 */
	public void setId(String id);

	/**
	 * 
	 * @return creation time
	 */
	public long getCreationTime();

	/**
	 * 
	 * @param created creation time
	 */
	public void setCreationTime(long created);

	/**
	 * 
	 * @return last accessed time
	 */
	public long getAccessed();

	/**
	 * 
	 * @param accessed last accessed time
	 */
	public void setAccessed(long accessed);

	/**
	 * 
	 * @return attributes
	 */
	public Map<String, Object> getAttributeMap();

	/**
	 * 
	 * @param attributes
	 */
	public void setAttributeMap(Map<String, Object> attributes);

	/**
	 * 
	 * @param key of attribute
	 * @return attribute value
	 */
	public Object getAttribute(String key);

	/**
	 * 
	 * @param key key of attribute
	 * @param obj attribute value
	 */
	public void setAttribute(String key, Object obj);

	/**
	 * 
	 * @param key of attribute
	 */
	public void removeAttribute(String key);

	/**
	 * 
	 * @return enumeration of attribute names
	 */
	public Enumeration<String> getAttributeNames();

	/**
	 * 
	 * @return true if session is valid
	 */
	public boolean isValid();

	/**
	 * 
	 * @param valid set true for valid sessions
	 */
	public void setValid(boolean valid);

	/**
	 * 
	 * @return invalidated time
	 */
	public long getInvalidated();

	/**
	 * 
	 * @return version
	 */
	public long getVersion();

	/**
	 * 
	 * @param version
	 */
	public void setVersion(long version);

	/**
	 * 
	 * @return domain which the session was bound for
	 */
	public String getDomain();

	/**
	 * 
	 * @param domain which the session was bound for
	 */
	public void setDomain(String domain);

	/**
	 * 
	 * @return path which the session was bound for
	 */
	public String getPath();

	/**
	 * 
	 * @param path which the session was bound for
	 */
	public void setPath(String path);
}
