package org.eclipse.jetty.server.session;

import org.eclipse.jetty.server.SessionManager;

/**
 * Created by ryoco on 2017/04/26.
 */
public class KeyValueStoreSessionHandler extends SessionHandler {

    private SessionManager _sessionManager;

    public KeyValueStoreSessionHandler(SessionManager manager) {
        setSessionManager(manager);
    }

        /* ------------------------------------------------------------ */

    /**
     * @return Returns the sessionManager.
     */
    public SessionManager getSessionManager() {
        return _sessionManager;
    }

    /* ------------------------------------------------------------ */

    /**
     * @param sessionManager The sessionManager to set.
     */
    public void setSessionManager(SessionManager sessionManager) {
        if (isStarted())
            throw new IllegalStateException();
        if (sessionManager != null)
            sessionManager.setSessionHandler(this);
        updateBean(_sessionManager, sessionManager);
        _sessionManager = sessionManager;
    }

}
