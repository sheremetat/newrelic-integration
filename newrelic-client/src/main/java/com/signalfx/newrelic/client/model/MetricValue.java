package com.signalfx.newrelic.client.model;

public class MetricValue {
    public final double value;
    public final long startTimeInMillis;

    public MetricValue(double value, long startTimeInMillis) {
        this.value = value;
        this.startTimeInMillis = startTimeInMillis;
    }

    @Override
    public String toString() {
        return "MetricValue{" +
                "value=" + value +
                ", startTimeInMillis=" + startTimeInMillis +
                '}';
    }
}
