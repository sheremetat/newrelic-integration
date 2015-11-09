/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers;
import com.signalfx.newrelic.client.MetricDataRequest;
import com.signalfx.newrelic.process.reporter.Reporter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ReportNewRelicTest {

    @Test
    @Ignore
    /**
     * Normal process of retrieving and reporting metrics.
     */
    public void testProcess() throws Exception {
        /*AppInfo app = new AppInfo("any");
        app.metrics.add(new MetricInfo("A|B", "C", null));

        MetricData metricData = new MetricData("", 0L, "name", "A|B");
        metricData.metricValues.add(new MetricValue(1, 1, 1, 1, 1, 2));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.singletonList(metricData));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(Collections.singletonList(app),
                MetricDataRequest.TimeParams.beforeNow(1L));

        Map<String, String> expectedDimensions = getExpectedDimensions();
        expectedDimensions.put("C", "A");

        Mockito.verify(request, Mockito.times(1)).get();
        Mockito.verify(reporter, Mockito.times(1)).report(
                Collections.singletonList(getDataPoint("B", 2, 1, expectedDimensions)));
        assertEquals(1,
                metricRegistry.counter(StatusType.dataPointsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.mtsEmpty.name()).getCount());
        assertEquals(1,
                metricRegistry.counter(StatusType.mtsReported.name()).getCount());
        assertEquals(0,
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());*/
    }

    @Test
    @Ignore
    /**
     * Unable to retrieve metrics from NewRelic.
     */
    public void testMetricRequestFailure() throws Exception {
        /*AppInfo app = new AppInfo("any");
        app.metrics.add(new MetricInfo("A|B", "C", null));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenThrow(new RequestException(""));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(
                ReportNewRelic.class);
        reportAppD.perform(Collections.singletonList(app),
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
    */}

    @Test
    @Ignore
    /**
     * Request to NewRelic was not authorized.
     */
    public void testMetricRequestUnauthorized() throws Exception {
        /*AppInfo app = new AppInfo("any");
        app.metrics.add(new MetricInfo("A|B", "C", null));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenThrow(new UnauthorizedException(""));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(Collections.singletonList(app),
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
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());*/
    }

    @Test
    @Ignore
    /**
     * Submitting metrics to SignalFx failure.
     */
    public void testProcessReportFailure() throws Exception {
        /*AppInfo app = new AppInfo("any");
        app.metrics.add(new MetricInfo("A|B", "C", null));

        MetricData metricData = new MetricData("", 0L, "name", "A|B");
        metricData.metricValues.add(new MetricValue(1, 1, 1, 1, 1, 2));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.singletonList(metricData));

        Reporter reporter = Mockito.mock(Reporter.class);
        Mockito.doThrow(new Reporter.ReportException("Something", null))
                .when(reporter).report(Mockito.anyList());

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(Collections.singletonList(app),
                MetricDataRequest.TimeParams.beforeNow(10L));

        Map<String, String> expectedDimensions = getExpectedDimensions();
        expectedDimensions.put("C", "A");

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
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());*/
    }

    @Test
    @Ignore
    /**
     * Normal process with metrics with empty value list from NewRelic.
     */
    public void testProcessMetricEmpty() throws Exception {
        /*AppInfo app = new AppInfo("any");
        app.metrics.add(new MetricInfo("A|B", "C", null));

        MetricData metricData = new MetricData("", 0L, "name", "A|B");

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.singletonList(metricData));

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(
                ReportNewRelic.class);
        reportAppD.perform(Collections.singletonList(app),
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
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());*/
    }

    @Test
    @Ignore
    /**
     * Normal process with empty list of metrics from NewRelic.
     */
    public void testProcessNoMetrics() throws Exception {
        /*AppInfo app = new AppInfo("any");
        app.metrics.add(new MetricInfo("A|B", "C", null));

        MetricDataRequest request = Mockito.mock(MetricDataRequest.class);
        Mockito.when(request.get()).thenReturn(Collections.<MetricData>emptyList());

        Reporter reporter = Mockito.mock(Reporter.class);

        MetricRegistry metricRegistry = new MetricRegistry();

        ReportNewRelic reportAppD = Guice.createInjector(
                new AppDReportTestModule(request, reporter, metricRegistry)).getInstance(ReportNewRelic.class);
        reportAppD.perform(Collections.singletonList(app),
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
                metricRegistry.counter(StatusType.newRelicRequestFailure.name()).getCount());*/
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
                        .setIntValue(value)).build();
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
