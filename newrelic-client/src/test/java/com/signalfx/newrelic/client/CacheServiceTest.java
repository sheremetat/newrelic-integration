/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client;

import com.signalfx.newrelic.client.cache.CacheService;
import com.signalfx.newrelic.client.cache.Principal;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CacheServiceTest {
    private CacheService cacheService;
    private Server server;
    private NewRelicTestHandler newRelicTestHandler;

    @Before
    public void setUp() throws Exception {
        cacheService = CacheService.create();

        newRelicTestHandler = new NewRelicTestHandler();

        server = new Server(0);
        server.setHandler(newRelicTestHandler);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        if (cacheService != null) {
            cacheService.invalidateCache();
        }

        if (server != null) {
            server.stop();
        }
        server = null;
    }

    @Test
    public void testIsExpired() throws Exception {
        String type = "applications";
        String type2 = "servers";
        assertTrue(cacheService.isExpired(type));
        assertTrue(cacheService.isExpired(type2));

        final int port = server.getConnectors()[0].getLocalPort();
        cacheService.updateCache("http://localhost:" + port,
                type, "token");

        assertFalse(cacheService.isExpired(type));
        assertTrue(cacheService.isExpired(type2));
    }

    @Test
    public void testUpdateCache() throws Exception {
        String type = "applications";

        final int port = server.getConnectors()[0].getLocalPort();
        cacheService.updateCache("http://localhost:" + port,
                type, "token");

        List<Principal> principals = cacheService.readPrincipals(type);
        assertEquals(1, principals.size());
    }

    @Test
    public void testReadPrincipals() throws Exception {
        String type = "applications";

        final int port = server.getConnectors()[0].getLocalPort();
        cacheService.updateCache("http://localhost:" + port,
                type, "token");

        Principal expectedPrincipal = new Principal(type, "10452894");

        String[] expectedMetrics = {"Custom/EventType/hits"};
        expectedPrincipal.updateMetrics(Arrays.asList(expectedMetrics));

        List<Principal> principals = cacheService.readPrincipals(type);
        assertEquals(1, principals.size());
        assertEquals(expectedPrincipal, principals.get(0));
    }

    private class NewRelicTestHandler extends AbstractHandler {

        private int status = HttpStatus.OK_200;

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response)
                throws IOException, ServletException {

            String responseString;
            String mockFile = "/" + target.split("/")[target.split("/").length - 1];

            responseString =
                    IOUtils.toString(
                            getClass().getResourceAsStream(
                                    String.format(mockFile)));
            response.setStatus(status);
            response.getWriter().write(responseString);
            baseRequest.setHandled(true);
        }
    }
}
