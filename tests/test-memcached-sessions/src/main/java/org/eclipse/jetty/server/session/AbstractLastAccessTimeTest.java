//
//  ========================================================================
//  Copyright (c) 1995-2017 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.server.session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;


/**
 * AbstractLastAccessTimeTest
 *
 * This test checks that a session can migrate from node A to node B, kept in use in node B
 * past the time at which it would have expired due to inactivity on node A but is NOT
 * scavenged by node A. In other words, it tests that a session that migrates from one node
 * to another is not timed out on the original node.
 */
public abstract class AbstractLastAccessTimeTest
{
    public abstract AbstractTestServer createServer(int port, int max, int scavenge);

    @Test
    public void testLastAccessTime() throws Exception
    {
        String contextPath = "";
        String servletMapping = "/server";
        int maxInactivePeriod = 8; //session will timeout after 8 seconds
        int scavengePeriod = 2; //scavenging occurs every 2 seconds
        AbstractTestServer server1 = createServer(0, maxInactivePeriod, scavengePeriod);
        TestServlet servlet1 = new TestServlet();
        ServletHolder holder1 = new ServletHolder(servlet1);
        ServletContextHandler context = server1.addContext(contextPath);
        TestSessionListener listener1 = new TestSessionListener();
        context.addEventListener(listener1);
        context.addServlet(holder1, servletMapping);

        try
        {
            server1.start();
            int port1=server1.getPort();
            AbstractTestServer server2 = createServer(0, maxInactivePeriod, scavengePeriod);
            server2.addContext(contextPath).addServlet(TestServlet.class, servletMapping);

            try
            {
                server2.start();
                int port2=server2.getPort();
                HttpClient client = new HttpClient();
                client.start();
                try
                {
                    // Perform one request to server1 to create a session
                    ContentResponse response1 = client.GET("http://localhost:" + port1 + contextPath + servletMapping + "?action=init");
                    assertEquals(HttpServletResponse.SC_OK, response1.getStatus());
                    assertEquals("test", response1.getContentAsString());
                    String sessionCookie = response1.getHeaders().get("Set-Cookie");
                    assertTrue( sessionCookie != null );
                    // Mangle the cookie, replacing Path with $Path, etc.
                    sessionCookie = sessionCookie.replaceFirst("(\\W)(P|p)ath=", "$1\\$Path=");

                    // Perform some request to server2 using the session cookie from the previous request
                    // This should migrate the session from server1 to server2, and leave server1's
                    // session in a very stale state, while server2 has a very fresh session.
                    // We want to test that optimizations done to the saving of the shared lastAccessTime
                    // do not break the correct working
                    int requestInterval = 500;
                    for (int i = 0; i < maxInactivePeriod * (1000 / requestInterval); ++i)
                    {
                        Request request = client.newRequest("http://localhost:" + port2 + contextPath + servletMapping);
                        request.header("Cookie", sessionCookie);
                        ContentResponse response2 = request.send();
                        assertEquals(HttpServletResponse.SC_OK , response2.getStatus());
                        assertEquals("test", response2.getContentAsString());

                        String setCookie = response2.getHeaders().get("Set-Cookie");
                        if (setCookie!=null)
                            sessionCookie = setCookie.replaceFirst("(\\W)(P|p)ath=", "$1\\$Path=");

                        Thread.sleep(requestInterval);
                    }

                    // At this point, session1 should be eligible for expiration.
                    // Let's wait for the scavenger to run, waiting 2.5 times the scavenger period
                    Thread.sleep(scavengePeriod * 2500L);

                    //check that the session was not scavenged over on server1 by ensuring that the SessionListener destroy method wasn't called
                    assertFalse(listener1.destroyed);
                }
                finally
                {
                    client.stop();
                }
            }
            finally
            {
                server2.stop();
            }
        }
        finally
        {
            server1.stop();
        }
    }

    public static class TestSessionListener implements HttpSessionListener
    {
        public boolean destroyed = false;
        public boolean created = false;

        @Override
        public void sessionDestroyed(HttpSessionEvent se)
        {
           destroyed = true;
        }

        @Override
        public void sessionCreated(HttpSessionEvent se)
        {
            created = true;
        }
    }



    public static class TestServlet extends HttpServlet
    {


        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse httpServletResponse) throws ServletException, IOException
        {
            String action = request.getParameter("action");
            if ("init".equals(action))
            {
                HttpSession session = request.getSession(true);
                session.setAttribute("test", "test");
                sendResult(session, httpServletResponse.getWriter());

            }
            else
            {
                HttpSession session = request.getSession(false);

                // if we node hopped we should get the session and test should already be present
                sendResult(session, httpServletResponse.getWriter());

                if (session!=null)
                {
                    session.setAttribute("test", "test");
                }
            }
        }

        private void sendResult(HttpSession session, PrintWriter writer)
        {
                if (session != null)
                {
                        writer.print(session.getAttribute("test"));
                }
                else
                {
                        writer.print("null");
                }
        }
    }
}
