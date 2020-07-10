package org.eclipse.jetty.nosql.memcached;

import org.eclipse.jetty.nosql.kvs.session.AbstractSessionFactory;
import org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFactory;
import org.eclipse.jetty.nosql.memcached.spymemcached.SpyMemcachedClientFactory;
import org.eclipse.jetty.server.session.AbstractSessionDataStoreFactory;
import org.eclipse.jetty.server.session.SessionDataStore;
import org.eclipse.jetty.server.session.SessionHandler;

/**
 * Created by ryoco on 2017/04/28.
 */
public class MemcachedSessionDataStoreFactory extends AbstractSessionDataStoreFactory {

    AbstractSessionFactory _sessionFactory;
    AbstractMemcachedClientFactory _clientFactory;
    String _serverString;
    int _defaultExpiry = 30;
    int _timeoutInMs = 1000;

    public MemcachedSessionDataStoreFactory()
    {
        this("localhost:11211");
    }

    public MemcachedSessionDataStoreFactory(String _serverString)
    {
        this._serverString = _serverString;
        this._sessionFactory = new SerializableSessionFactory(); // default
        this._clientFactory = new SpyMemcachedClientFactory(); // default
    }



    public void setSessionFactory(AbstractSessionFactory _sessionFactory) {
        this._sessionFactory = _sessionFactory;
    }

    public void setClientFactory(AbstractMemcachedClientFactory _clientFactory) {
        this._clientFactory = _clientFactory;
    }


    public void setServerString(String _serverString) {
        this._serverString = _serverString;
    }

    public void setDefaultExpiry(int _defaultExpiry) {
        this._defaultExpiry = _defaultExpiry;
    }

    public void setTimeoutInMs(int _timeoutInMs) {
        this._timeoutInMs = _timeoutInMs;
    }

    @Override
    public SessionDataStore getSessionDataStore(SessionHandler handler) throws Exception {
        MemcachedSessionDataStore ds = new MemcachedSessionDataStore();

        ds.setSessionFactory(_sessionFactory);
        ds.setClientFactory(_clientFactory);
        ds.setServerString(_serverString);

        ds.setExpiry(_defaultExpiry);
        ds.setTimeoutInMs(_timeoutInMs);
        ds.setGracePeriodSec(getGracePeriodSec());
        ds.setSavePeriodSec(getSavePeriodSec());

        return ds;
    }

}
