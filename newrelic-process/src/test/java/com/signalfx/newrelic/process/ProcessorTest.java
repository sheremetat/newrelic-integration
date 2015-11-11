/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.signalfx.newrelic.client.model.MetricValue;
import com.signalfx.newrelic.process.model.MetricTimeSeries;
import com.signalfx.newrelic.process.processor.Processor;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers;

public class ProcessorTest {

    @Test
    public void testProcessor() {
        List<MetricValue> metricValues = new LinkedList<>();
        metricValues.add(new MetricValue(9,1));
        metricValues.add(new MetricValue(8,2));
        metricValues.add(new MetricValue(7,3));

        MetricTimeSeries mts1 = new MetricTimeSeries("1", null);
        MetricTimeSeries mts2 = new MetricTimeSeries("2", null);

        Processor processor = new Processor();

        // Process normal data
        List<SignalFxProtocolBuffers.DataPoint> dataPoints = processor.process(mts1, metricValues);

        List<SignalFxProtocolBuffers.DataPoint> expectedDataPoints =
                Lists.newArrayList(
                        getDataPoint("1", 1, 9),
                        getDataPoint("1", 2, 8),
                        getDataPoint("1", 3, 7));
        assertEquals(expectedDataPoints, dataPoints);

        metricValues.clear();

        // Same mts, overlapping timestamp
        metricValues.add(new MetricValue(6, 3));
        metricValues.add(new MetricValue(4, 4));
        dataPoints = processor.process(mts1, metricValues);
        expectedDataPoints = Lists.newArrayList(getDataPoint("1", 4, 4));
        assertEquals(expectedDataPoints, dataPoints);

        // Different mts, overlap
        dataPoints = processor.process(mts2, metricValues);
        expectedDataPoints = Lists.newArrayList(getDataPoint("2", 3, 6), getDataPoint("2", 4, 4));
        assertEquals(expectedDataPoints, dataPoints);
    }

    private SignalFxProtocolBuffers.DataPoint getDataPoint(String metricName, long timestamp,
                                                           long value) {
        return SignalFxProtocolBuffers.DataPoint
                .newBuilder().setMetric(metricName).setTimestamp(timestamp).setValue(
                        SignalFxProtocolBuffers.Datum.newBuilder()
                                .setDoubleValue(value)).build();
    }
}
