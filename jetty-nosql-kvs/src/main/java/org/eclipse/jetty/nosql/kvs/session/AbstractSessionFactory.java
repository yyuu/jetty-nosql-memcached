package org.eclipse.jetty.nosql.kvs.session;

import java.util.Enumeration;

import org.eclipse.jetty.server.session.AbstractSession;

public abstract class AbstractSessionFactory {
	public abstract ISerializableSession create();

	public ISerializableSession create(String sessionId) {
		ISerializableSession s = create();
		s.setId(sessionId);
		return s;
	}

	public ISerializableSession create(String sessionId, long created) {
		ISerializableSession s = create(sessionId);
		s.setCreationTime(created);
		return s;
	}

	public ISerializableSession create(AbstractSession session) {
		ISerializableSession s = create(session.getId(), session.getCreationTime());
		s.setAccessed(session.getAccessed());
		for (Enumeration<String> e=session.getAttributeNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			s.setAttribute(key, session.getAttribute(key));
		}
		s.setValid(session.isValid());
		return s;
	}
}
