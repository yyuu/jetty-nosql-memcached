package org.eclipse.jetty.nosql.memcached;

// ========================================================================
// Copyright (c) 1996-2009 Mort Bay Consulting Pty. Ltd.
// Copyright (c) 2012 Geisha Tokyo Entertainment, Inc.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

import org.eclipse.jetty.server.session.AbstractReentrantRequestSessionTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.junit.Test;

/**
 * ReentrantRequestSessionTest
 */
public abstract class AbstractMemcachedReentrantRequestSessionTest extends AbstractReentrantRequestSessionTest
{
	private Logger log = Log.getLogger("org.eclipse.jetty.nosql.memcached.AbstractMemcachedReentrantRequestSessionTest");

	public AbstractTestServer createServer(int port)
	{
		return new MemcachedTestServer(port);
	}

	@Test
	public void testReentrantRequestSession() throws Exception
	{
		super.testReentrantRequestSession();
	}
}
