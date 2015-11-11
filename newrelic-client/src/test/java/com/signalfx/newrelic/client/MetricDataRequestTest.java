/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.signalfx.newrelic.client.exception.RequestException;
import com.signalfx.newrelic.client.exception.UnauthorizedException;
import com.signalfx.newrelic.client.model.MetricData;
import com.signalfx.newrelic.client.model.MetricValue;

public class MetricDataRequestTest {

    private Server server;
    private NewRelicTestHandler newRelicTestHandler;

    @Before
    public void setUp() throws Exception {
        newRelicTestHandler = new NewRelicTestHandler();

        server = new Server(0);
        server.setHandler(newRelicTestHandler);
        server.start();
    }

    @After
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        server = null;
    }

    @Test
    public void testGetMetric() throws Exception {
        MetricDataRequest metricDataRequest = getMetricDataRequest();
        List<MetricData> metricDataList = metricDataRequest.get();

        Collections.sort(metricDataList, new Comparator<MetricData>() {
            @Override
            public int compare(MetricData o1, MetricData o2) {
                return o2.metricName.compareTo(o1.metricName);
            }
        });

        System.out.println(metricDataList);

        // New Metric name is calculated by formula "NEWRELIC metric name" + "/" + "value"
        // In case of 6 values we will have 6 metrics
        assertEquals(6, metricDataList.size());

        MetricData metricData = metricDataList.get(2);
        assertEquals("Custom/EventType/hits/max_value",
                metricData.metricName);
        assertEquals(1, metricData.metricValues.size());

        MetricValue metricValue = metricData.metricValues.get(0);
        assertEquals(1447179120000L, metricValue.startTimeInMillis);
        assertEquals(0.964f, metricValue.value, 0.001);
    }

    @Test
    public void testGetMetricUnauthorized() throws Exception {
        newRelicTestHandler.setStatus(HttpStatus.UNAUTHORIZED_401);
        MetricDataRequest metricDataRequest = getMetricDataRequest();
        try {
            metricDataRequest.get();
            fail("Unauthorized Exception Expected");
        } catch (UnauthorizedException e) {
            // Expected
        }
    }

    @Test
    public void testGetMetricUnhandled() throws Exception {
        newRelicTestHandler.setStatus(HttpStatus.FORBIDDEN_403);
        MetricDataRequest metricDataRequest = getMetricDataRequest();
        try {
            metricDataRequest.get();
            fail("Request Exception Expected");
        } catch (RequestException e) {
            // Expected
        }
    }

    @Test
    public void testServerNotAvailable() throws Exception {
        MetricDataRequest metricDataRequest = getMetricDataRequest();
        server.stop();
        try {
            metricDataRequest.get();
            fail("Request Exception Expected");
        } catch (RequestException e) {
            //Expected
        }
    }

    private MetricDataRequest getMetricDataRequest() {
        final int port = server.getConnectors()[0].getLocalPort();
        MetricDataRequest metricDataRequest = new MetricDataRequest("http://localhost:" + port,
                "token");
        metricDataRequest.setAppName("applications");
        metricDataRequest.setTimeParams(MetricDataRequest.TimeParams.beforeNow(2));
        return metricDataRequest;
    }

    @Test
    public void testTimeParams() throws Exception {
        assertEquals(new MetricDataRequest.TimeParams("AFTER_TIME", 100, 200, 0),
                MetricDataRequest.TimeParams.afterTime(100, 200));
        assertEquals(new MetricDataRequest.TimeParams("BEFORE_NOW", 100, System.currentTimeMillis() - 100 * 60 * 1000, System.currentTimeMillis()),
                MetricDataRequest.TimeParams.beforeNow(100));
        assertEquals(new MetricDataRequest.TimeParams("BEFORE_TIME", 100, 0, 200),
                MetricDataRequest.TimeParams.beforeTime(100, 200));
        assertEquals(new MetricDataRequest.TimeParams("BETWEEN_TIMES", 0, 100, 200),
                MetricDataRequest.TimeParams.betweenTime(100, 200));
    }

    private class NewRelicTestHandler extends AbstractHandler {

        private int status = HttpStatus.OK_200;

        public void setStatus(int status) {
            this.status = status;
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request,
                           HttpServletResponse response)
                throws IOException, ServletException {

            String responseString;
            if (status != HttpStatus.OK_200) {
                String mockFile = "/metric_response_" + status + ".json";

                responseString =
                        IOUtils.toString(
                                getClass().getResourceAsStream(
                                        String.format(mockFile, status)));
            } else {
                String mockFile = "/" + target.split("/")[target.split("/").length - 1];

                responseString =
                        IOUtils.toString(
                                getClass().getResourceAsStream(
                                        String.format(mockFile, status)));
            }
            response.setStatus(status);
            response.getWriter().write(responseString);
            baseRequest.setHandled(true);
        }
    }
}
