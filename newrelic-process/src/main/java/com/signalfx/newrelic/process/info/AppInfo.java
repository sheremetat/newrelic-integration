/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process.info;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signalfx.newrelic.client.cache.CacheService;
import com.signalfx.newrelic.client.cache.Database;
import com.signalfx.newrelic.process.model.MetricTimeSeries;

/**
 * AppInfo representing NewRelic App.
 *
 * @author 9park
 */
public class AppInfo {
    public final List<String> domains;

    @JsonCreator
    public AppInfo(@JsonProperty("domains") List<String> domains) {
        this.domains = domains;
    }

    @Override
    public String toString() {
        return String.format("domains: %s", domains.toString());
    }

    /**
     * Create a {@link MetricTimeSeries} for a given metric path. It will use the given metric path
     * as dimensions mapping and add extra dimensions as specified in the MetricInfo.
     *
     * @param actualMetricPath
     *         metric path used to mapped to dimensions in metric time series.
     * @return {@link MetricTimeSeries}
     */
    public MetricTimeSeries getMetricTimeSeries(String actualMetricPath, HashMap<String,String> customDimensions) {
        Map<String, String> actualDimensions = new HashMap<>();

        if(customDimensions != null && customDimensions.size() > 0){
            actualDimensions.putAll(customDimensions);
        }

        // Always add metric_source as NewRelic.
        actualDimensions.put("metric_source", "NewRelic");

        return new MetricTimeSeries(actualMetricPath, actualDimensions);
    }
}
