# jetty-nosql-memcached

[![Build Status](https://secure.travis-ci.org/yyuu/jetty-nosql-memcached-parent.png?branch=master)](http://travis-ci.org/yyuu/jetty-nosql-memcached-parent)

## Overview

SessionManager implementation for Jetty based on jetty-nosql.


## Requirements

All dependencies can be resolved from Maven.

* jetty (8.0.0 or later)
* spymemcached (for MemcachedSessionManager)


## Build

See also ./BUILDING.txt


## Configuration

You need to configure both "session manager" and "session ID manager".


### Configuring "session ID manager"

SessionIdManagers can be configured in files under ${JETTY_HOME}/etc.
In following example, using ${JETTY_HOME}/etc/jetty.xml.

    <?xml version="1.0"?>
    <Configure id="Server" class="org.eclipse.jetty.server.Server">
      
      (... snip ...)
      
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
    <!-- // equivalent in Java:
      Server server = ...;
      MemcachedSessionIdManager memcachedSessionIdManager = new MemcachedSessionIdManager(server);
      memcachedSessionIdManager.setServerString("localhost:11211");
      memcachedSessionIdManager.setKeyPrefix("session:");
      server.setSessionIdManager(memcachedSessionIdManager);
      server.setAttribute("memcachedSessionIdManager", memcachedSessionIdManager);
      -->
    </Configure>

#### Extra options for "session ID manager"

You can configure the behavior of session ID manager with following setters.

* setDefaultExpiry(int defaultExpiry)
  * set default expiry of sessions on memcached.
* setKeyPrefix(String keyPrefix)
  * use keyPrefix for session key prefix on memcached.
* setKeySuffix(String keySuffix)
  * use keySuffix for session key suffix on memcached.
* setServerString(String serverString)
  * specify server address and port in string. multiple hosts can be specified with spaces.
* setTimeoutInMs(int timeoutInMS)
  * set timeout for memcached connections.


### Configuring "session manager"

SessionManagers can be configured by either ${APP_ROOT}/WEB-INF/jetty-web.xml or ${JETTY_HOME}/context/${APP_NAME}.xml

Sample configuration for ${APP_ROOT}/WEB-INF/jetty-web.xml:

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
    <!-- // equivalent in Java
      WebAppContext context = ...
      Server server = ...;
      MemcachedSessionIdManager sessionIdManager = server.getAttribute("memcachedSessionIdManager");
      MemcachedSessionManager sessionManager = new MemcachedSessionManager();
      sessionManager.setSessionIdManager(sessionIdManager);
      context.setSessionHandler(new SessionHandler(sessionManager));
      -->

Sample configuration for ${JETTY_HOME}/context/${APP_NAME}.xml:

    <?xml version="1.0"  encoding="ISO-8859-1"?>
    <!DOCTYPE Configure PUBLIC "-//Mort Bay Consulting//DTD Configure//EN" "http://jetty.eclipse.org/configure.dtd">
    <Configure class="org.eclipse.jetty.webapp.WebAppContext">
      
      (... snip ...)
      
      <Ref name="Server" id="Server">
        <Call id="sessionIdManager" name="getAttribute">
          <Arg>memcachedSessionIdManager</Arg>
        </Call>
      </Ref>
      <Set name="sessionHandler">
        <New class="org.eclipse.jetty.server.session.SessionHandler">
          <Arg>
            <New id="memcachedSessionManaegr" class="org.eclipse.jetty.nosql.memcached.MemcachedSessionManager">
              <Set name="sessionIdManager">
                <Ref id="memcachedSessionIdManager" />
              </Set>
            </New>
          </Arg>
        </New>
      </Set>
    </Configure>
    <!-- // equivalent in Java
      WebAppContext context = ...
      Server server = ...;
      MemcachedSessionIdManager sessionIdManager = server.getAttribute("memcachedSessionIdManager");
      MemcachedSessionManager sessionManager = new MemcachedSessionManager();
      sessionManager.setSessionIdManager(sessionIdManager);
      context.setSessionHandler(new SessionHandler(sessionManager));
      -->


#### Extra options for "session manager"

You can configure the behavior of session manager with following setters.

* setSessionIdManager(SessionIdManager idManager)
** session id manager you created.
* setSessionFacade(AbstractSessionFacade sf)
** set session serializer. org.eclipse.jetty.nosql.kvs.session.serializable.SerializableSessionFacade is used by default.


## License

Copyright (c) 2011 Geisha Tokyo Entertainment, Inc.

All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
and Apache License v2.0 which accompanies this distribution.

The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html

The Apache License v2.0 is available at http://www.opensource.org/licenses/apache2.0.php

You may elect to redistribute this code under either of these licenses.


## Author

Copyright (C) 2011-2012 Geisha Tokyo Entertainment, Inc.

Copyright (C) 2011-2012 Yamashita, Yuu <yamashita@geishatokyo.com>
