# jetty-nosql-memcached

## Overview

Jetty's SessionManager implementation based on jetty-nosql.


## Requirements

* jetty (8.0.0 or later)
* jetty-nosql (8.0.0 or later)
* spymemcached (for MemcachedSessionManager)

You may need following dependency to run tests.

* org.eclipse.jetty.tests:test-sessions-common


## Configuration (MemcachedSessionManager)

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
          <Set name="serverString">localhost:11211</Set>
          <Set name="keyPrefix">session:</Set>
        </New>
      </Set>
      <Call name="setAttribute">
        <Arg>memcachedSessionIdManager</Arg>
        <Arg><Ref id="memcachedSessionIdManager" /></Arg>
      </Call>
    <!--
      Server server = new Server();
      MemcachedSessionIdManager memcachedSessionIdManager = new MemcachedSessionIdManager(server);
      memcachedSessionIdManager.setServerString("localhost:11211");
      memcachedSessionIdManager.setKeyPrefix("session:");
      server.setSessionIdManager(memcachedSessionIdManager);
      -->
    </Configure>


Configuring MemcachedSessionManager in ${APP_ROOT}/WEB-INF/jetty-web.xml.

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

## License

Copyright (c) 2011 Geisha Tokyo Entertainment, Inc.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
and Apache License v2.0 which accompanies this distribution.

The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html

The Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php

You may elect to redistribute this code under either of these licenses.


## Author

Copyright (C) 2011 Geisha Tokyo Entertainment, Inc.

Yamashita, Yuu <yamashita@geishatokyo.com>
