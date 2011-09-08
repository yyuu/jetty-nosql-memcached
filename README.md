# jetty-nosql-memcached

## Overview

Jetty's SessionManager implementation based on jetty-nosql.


## Requirements

* jetty (8.0.0 or later)
* jetty-nosql (8.0.0 or later)
* spymemcached

You may need following dependency to run tests.

* org.eclipse.jetty.tests:test-sessions-common


## Configuration

Configuring MemcachedSessionIdManager in ${JETTY_HOME}/etc/jetty.xml.

    <?xml version="1.0"?>
    <Configure id="Server" class="org.eclipse.jetty.server.Server">

    (... snip ...)

      <!-- =========================================================== -->
      <!-- org.eclipse.jetty.nosql.memcached.MemcachedSessionIdManager -->
      <!-- =========================================================== -->
      <Set name="sessionIdManager">
        <New id="memcachedSessionIdManager" class="org.eclipse.jetty.nosql.memcached.MemcachedSessionIdManager">
          <Arg><Ref id="Server" /></Arg>
          <Set name="memcachedServerString">localhost:11211</Set>
          <Set name="memcachedKeyPrefix">session:</Set>
        </New>
      </Set>
      <Call name="setAttribute">
        <Arg>memcachedSessionIdManager</Arg>
        <Arg><Ref id="memcachedSessionIdManager" /></Arg>
      </Call>
    <!--
      Server server = new Server();
      MemcachedSessionIdManager memcachedSessionIdManager = new MemcachedSessionIdManager(server);
      memcachedSessionIdManager.setMemcachedServerString("localhost:11211");
      memcachedSessionIdManager.setMemcachedKeyPrefix("session:");
      server.setSessionIdManager(memcachedSessionIdManager);
      -->
    </Configure>


Configuring MemcachedSessionManager in WEB-INF/jetty-web.xml.

    <?xml version="1.0" encoding="UTF-8"?>
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">

    (... snip ...)

      <Get name="server">
        <Get id="memcachedSessionIdManager" name="sessionIdManager" />
      </Get>
      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New class="org.eclipse.jetty.nosql.memcached.MemcachedSessionManager">
              <Set name="sessionIdManager">
                <Ref id="memcachedSessionIdManager" />
              </Set>
            </New>
          </Arg>
        </New>
      </Set>
    </Configure>


## Author

Geisha Tokyo Entertainment, Inc.
Yamashita, Yuu <yamashita@geishatokyo.com>
