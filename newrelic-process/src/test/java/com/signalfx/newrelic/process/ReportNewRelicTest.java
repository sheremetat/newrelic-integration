/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers;
import com.signalfx.newrelic.client.MetricDataRequest;
import com.signalfx.newrelic.client.exception.RequestException;
import com.signalfx.newrelic.client.exception.UnauthorizedException;
import com.signalfx.newrelic.client.model.MetricData;
import com.signalfx.newrelic.client.model.MetricValue;
import com.signalfx.newrelic.process.info.AppInfo;
import com.signalfx.newrelic.process.reporter.Reporter;
import com.signalfx.newrelic.process.status.StatusType;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ReportNewRelicTest {

    @Test
    /**
     * Normal process of retrieving and reporting metrics.
     */
    public void testProcess() throws Exception {
        String[] domains = {"applications"};
        AppInfo app = new AppInfo(Arrays.asList(domains));

        MetricData metricData = new MetricData("name");
        metricData.metricValues.add(new MetricValue(1, 2));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.singletonList(metricData));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(app,
                MetricDataRequest.TimeParams.beforeNow(1L));

        Map<String, String> expectedDimensions = getExpectedDimensions();
        expectedDimensions.put("metric_source", "NewRelic");

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter, Mockito.times(1)).report(
                Collections.singletonList(getDataPoint("name", 2, 1, expectedDimensions)));
        assertEquals(1,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(1,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());
    }

    @Test
    /**
     * Unable to retrieve metrics from NewRelic.
     */
    public void testMetricRequestFailure() throws Exception {
        String[] domains = {"applications"};
        AppInfo app = new AppInfo(Arrays.asList(domains));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenThrow(new RequestException(""));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(
                ReportNewRelic.class);
        reportAppD.perform(app,
                MetricDataRequest.TimeParams.beforeNow(10L));

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter, Mockito.never()).report(Mockito.anyList());
        assertEquals(0,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(1,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());
    }

    @Test
    /**
     * Request to NewRelic was not authorized.
     */
    public void testMetricRequestUnauthorized() throws Exception {
        String[] domains = {"applications"};
        AppInfo app = new AppInfo(Arrays.asList(domains));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenThrow(new UnauthorizedException(""));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(app,
                MetricDataRequest.TimeParams.beforeNow(10L));

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter,
                Mockito.never()).report(Mockito.anyList());
        assertEquals(0,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());
    }

    @Test
    /**
     * Submitting metrics to SignalFx failure.
     */
    public void testProcessReportFailure() throws Exception {
        String[] domains = {"applications"};
        AppInfo app = new AppInfo(Arrays.asList(domains));

        MetricData metricData = new MetricData("B");
        metricData.metricValues.add(new MetricValue(1, 2));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.singletonList(metricData));

        Reporter reporter = Mockito.mock(Reporter.class);
        Mockito.doThrow(new Reporter.ReportException("Something", null))
                .when(reporter).report(Mockito.anyList());

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(app,
                MetricDataRequest.TimeParams.beforeNow(10L));

        Map<String, String> expectedDimensions = getExpectedDimensions();
        expectedDimensions.put("metric_source", "NewRelic");

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter, Mockito.times(1)).report(
                Collections.singletonList(getDataPoint("B", 2, 1, expectedDimensions)));
        assertEquals(0,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(1,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());
    }

    @Test
    /**
     * Normal process with metrics with empty value list from NewRelic.
     */
    public void testProcessMetricEmpty() throws Exception {
        String[] domains = {"applications"};
        AppInfo app = new AppInfo(Arrays.asList(domains));

        MetricData metricData = new MetricData("B");

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.singletonList(metricData));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(
                ReportNewRelic.class);
        reportAppD.perform(app,
                MetricDataRequest.TimeParams.beforeNow(10L));

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter,
                Mockito.never()).report(Mockito.anyList());
        assertEquals(0,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(1,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());
    }

    @Test
    /**
     * Normal process with empty list of metrics from NewRelic.
     */
    public void testProcessNoMetrics() throws Exception {
        String[] domains = {"applications"};
        AppInfo app = new AppInfo(Arrays.asList(domains));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.<MetricData>emptyList());

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(app,
                MetricDataRequest.TimeParams.beforeNow(10L));

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter,
                Mockito.never()).report(Mockito.anyList());
        assertEquals(0,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());
    }

    private SignalFxProtocolBuffers.DataPoint getDataPoint(String metricName, long timestamp,
                                                           long value,
                                                           Map<String, String> dimensions) {
        SignalFxProtocolBuffers.DataPoint.Builder builder = SignalFxProtocolBuffers.DataPoint
                .newBuilder();
        if (dimensions != null) {
            for (Map.Entry<String, String> entry : dimensions.entrySet()) {
                builder.addDimensions(SignalFxProtocolBuffers.Dimension.newBuilder()
                        .setKey(entry.getKey())
                        .setValue(entry.getValue()));
            }
        }
        return builder.setMetric(metricName).setTimestamp(timestamp).setValue(
                SignalFxProtocolBuffers.Datum.newBuilder()
                        .setDoubleValue(value)).build();
    }

    private Map<String, String> getExpectedDimensions() {
        Map<String, String> expectedDimensions = new HashMap<>();
        expectedDimensions.put("metric_source", "NewRelic");
        return expectedDimensions;
    }

    public static class AppDReportTestModule extends AbstractModule {

        private MetricDataRequest metricDataRequest;
        private Reporter reporter;
        private MetricRegistry metricRegistry;

        public AppDReportTestModule(MetricDataRequest metricDataRequest, Reporter reporter,
                                    MetricRegistry metricRegistry) {
            this.metricDataRequest = metricDataRequest;
            this.reporter = reporter;
            this.metricRegistry = metricRegistry;
        }

        @Override
        protected void configure() {
            bind(MetricDataRequest.class).toInstance(metricDataRequest);
            bind(Reporter.class).toInstance(reporter);
            bind(MetricRegistry.class).toInstance(metricRegistry);
        }
    }
}
