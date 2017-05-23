package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.nosql.NoSqlSessionDataStore;
import org.eclipse.jetty.nosql.kvs.AbstractKeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.IKeyValueStoreClient;
import org.eclipse.jetty.nosql.kvs.KeyValueStoreClientException;
import org.eclipse.jetty.nosql.kvs.session.AbstractSerializableSession;
import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClientFactory;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ryoco on 2017/04/28.
 * KeyValueStoreSessionManager
 */
public class MemcachedSessionDataStore extends NoSqlSessionDataStore {

    private final static Logger log = Log.getLogger(MemcachedSessionDataStore.class);

    private AbstractSessionFactory sessionFactory = null;
    private AbstractMemcachedClientFactory _clientFactory = null;
    private IKeyValueStoreClient _client = null;

    private long _defaultExpiry = TimeUnit.MINUTES.toMillis(30); // 30 minutes
    private int _timeoutInMs = 1000;
    private String _serverString = "";

    public MemcachedSessionDataStore() {
        super();
    }

    public AbstractSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(AbstractSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public AbstractMemcachedClientFactory getClientFactory() {
        return _clientFactory;
    }

    public void setClientFactory(AbstractMemcachedClientFactory _clientFactory) {
        this._clientFactory = _clientFactory;
    }

    public String getServerString() {
        return _serverString;
    }

    public void setServerString(String _serverString) {
        this._serverString = _serverString;
    }

    public int getExpiry() {
        return (int) TimeUnit.MILLISECONDS.toSeconds(_defaultExpiry);
    }

    public void setExpiry(int _defaultExpiry) {
        this._defaultExpiry = TimeUnit.SECONDS.toMillis(_defaultExpiry);
    }

    public int getTimeoutInMs() {
        return _timeoutInMs;
    }

    public void setTimeoutInMs(int _timeoutInMs) {
        this._timeoutInMs = _timeoutInMs;
    }

    @Override
    public void doStore(String id, SessionData data, long lastSaveTime) throws Exception {

        AbstractSerializableSession _data = (AbstractSerializableSession) data;

        if(lastSaveTime <= 0) {
            _data.setVersion(1);
        } else {
            _data.setVersion(_data.getVersion() + 1);
        }

        byte[] raw = getSessionFactory().pack(data);
        if (raw != null) {
            try {
                if (lastSaveTime <= 0) {
                    log.debug("add: id=" + id + ", lastSaveTime=" + lastSaveTime + ", expiry=" + getExpiry());
                    _client.add(mangleKey(id), raw, getExpiry());
                }
                else {
                    log.debug("set: id=" + id + ", lastSaveTime=" + lastSaveTime + ", expiry=" + getExpiry());
                    _client.set(mangleKey(id), raw, getExpiry());
                }
            }
            catch (KeyValueStoreClientException error) {
                log.warn("unable to add key: id=" + id, error);
            }
        }

    }

    @Override
    public Set<String> doGetExpired(Set<String> candidates) {
        Set<String> expiredSessions = new HashSet<>();
        for (String s: candidates) {
            try {
                if (!exists(s)){
                    expiredSessions.add(s);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return expiredSessions;
    }

    @Override
    public boolean isPassivating() {
        return true;
    }

    @Override
    public boolean exists(String id) throws Exception {
        return getKey(id) != null;
    }

    @Override
    public SessionData load(String id) throws Exception {
        byte[] raw = getKey(id);

        SessionData data = getSessionFactory().unpack(raw);

        if (data == null) {
            return null;
        }

        if (!id.equals(data.getId())) {
            log.warn("loadSession: invalid id (expected:" + id + ", got:" + data.getId() + ")");
            return null;
        }

        if (!_context.getVhost().equals(data.getVhost())) {
            log.warn("loadSession: invalid cookie domain (expected:" + _context.getVhost() + ", got:" + data.getVhost() + ")");
            return null;
        }

        if (!_context.getCanonicalContextPath().equals(data.getContextPath())) {
            log.warn("loadSession: invalid cookie path (expected:" + _context.getCanonicalContextPath() + ", got:" + data.getContextPath() + ")");
            return null;
        }

        AbstractSerializableSession session = newSessionData(
                data.getId(),
                data.getCreated(),
                data.getAccessed(),
                data.getLastAccessed(),
                data.getMaxInactiveMs());
        for (String attr : data.getAllAttributes().keySet()) {
            session.setAttribute(attr, data.getAttribute(attr));
        }
        session.setLastNode(data.getLastNode());
        session.setLastSaved(data.getLastSaved());

        return session;
    }

    @Override
    public boolean delete(String id) throws Exception {
        log.debug("delete: id=" + id);
        boolean result = false;
        try {
            result = _client.delete(mangleKey(id));
        }
        catch (KeyValueStoreClientException error) {
            log.warn("unable to delete key: id=" + id, error);
        }
        return result;
    }

    @Override
    public AbstractSerializableSession newSessionData(String id, long created, long accessed, long lastAccessed, long maxInactiveMs) {
        AbstractSerializableSession d = getSessionFactory().create(
                id,
                _context.getCanonicalContextPath(),
                _context.getVhost(),
                created,
                accessed,
                lastAccessed,
                maxInactiveMs);
        return d;
    }

    private byte[] getKey(final String id) {
        log.debug("get: id=" + id);
        byte[] raw = null;
        try {
            raw = _client.get(mangleKey(id));
        }
        catch (KeyValueStoreClientException error) {
            log.warn("unable to get key: id=" + id, error);
        }
        return raw;
    }

    private String mangleKey(final String idInCluster) {
        return idInCluster;
    }

    @Override
    protected void doStart() throws Exception {
        log.info("starting...");
        super.doStart();
        _client = newClient(_serverString);
        if (_client == null) {
            throw new IllegalStateException("newClient(" + _serverString + ") returns null.");
        }
        log.info("use " + _client.getClass().getSimpleName() + " as client factory.");
        _client.establish();
        log.info("started.");
    }

    @Override
    protected void doStop() throws Exception {
        log.info("stopping...");
        if (_client != null) {
            _client.shutdown();
            _client = null;
        }
        super.doStop();
        log.info("stopped.");
    }

    private AbstractKeyValueStoreClient newClient(String serverString) {
        synchronized (this) {
            if (_clientFactory == null) {
                _clientFactory = new SpyMemcachedClientFactory(); // default client
            }
        }
        AbstractKeyValueStoreClient client = _clientFactory.create(serverString);
        client.setTimeoutInMs(getTimeoutInMs());
        return client;
    }
}
