package com.signalfx.newrelic.client.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * MetricData represents each Metric data from NewRelic by metric's name.
 */
public class MetricData {

    public final String metricName;
    public final HashMap<String, String> dimensions;
    public final List<MetricValue> metricValues;

    public MetricData(String metricName) {
        this.metricName = metricName;
        this.metricValues = new LinkedList<>();
        this.dimensions = new HashMap<>();
    }

    @Override
    public String toString() {
        return "MetricData{" +
                "metricName='" + metricName + '\'' +
                ", metricValues=" + metricValues +
                '}';
    }
}
