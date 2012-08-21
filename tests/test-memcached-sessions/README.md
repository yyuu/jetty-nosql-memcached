# test-memcached-sessions

## Overview

Tests for jetty-nosql-memcached.


## Test

Use maven.

    $ mvn test


## Configuration

You can control test behavior by following system properties.

* `org.eclipse.jetty.nosql.memcached.clientFactory`
  * `spy`, `xmemcached` and `default` are sensible. `default` by default.
* `org.eclipse.jetty.nosql.memcached.servers`
  * specify memcached servers. use `127.0.0.1:11211` by default.
* `org.eclipse.jetty.nosql.memcached.useBinary`
  * use memcached binary protocols. `false` by default.
* `org.eclipse.jetty.nosql.memcached.useMock`
  * use memcached mock objects instead of actual memcached. `true` by default.
