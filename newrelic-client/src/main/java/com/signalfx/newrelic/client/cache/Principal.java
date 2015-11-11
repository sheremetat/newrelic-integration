/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client.cache;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity to persist metric to database
 */
public class Principal {
    /**
     * Type of metric group (servers, applications, browser_applications, etc)
     */
    private String type;

    /**
     * Id of group item (server ID or application ID, or ...)
     */
    private final String id;

    /**
     * List of metric names available for group
     */
    private final List<String> metrics;

    public Principal(String type, String id) {
        this.type = type;
        this.id = id;
        this.metrics = new ArrayList<>();
    }

    public void updateMetrics(List<String> metrics){
        this.metrics.clear();
        this.metrics.addAll(metrics);
    }

    public String getId() {
        return id;
    }

    public List<String> getMetrics() {
        return metrics;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Principal principal = (Principal) o;

        if (!type.equals(principal.type)) return false;
        if (!id.equals(principal.id)) return false;
        return metrics.equals(principal.metrics);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id.hashCode();
        result = 31 * result + metrics.hashCode();
        return result;
    }
}
