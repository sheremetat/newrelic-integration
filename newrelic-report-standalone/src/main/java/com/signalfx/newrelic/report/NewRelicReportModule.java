/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.report;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;
import com.signalfx.newrelic.client.MetricDataRequest;
import com.signalfx.newrelic.process.reporter.Reporter;
import com.signalfx.newrelic.report.config.ConnectionConfig;
import com.signalfx.newrelic.report.reporter.SignalFxRestReporter;

/**
 * Module configurations for NewRelicReport using given connection config.
 *
 * @author 9park
 */
public class NewRelicReportModule extends AbstractModule {

    private final ConnectionConfig connectionConfig;
    private final MetricRegistry metricRegistry;

    public NewRelicReportModule(ConnectionConfig connectionConfig, MetricRegistry metricRegistry) {
        this.connectionConfig = connectionConfig;
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected void configure() {
        bind(MetricDataRequest.class).toInstance(new MetricDataRequest(connectionConfig.newRelicURL, connectionConfig.newRelicApiKey));
        bind(Reporter.class).toInstance(new SignalFxRestReporter(connectionConfig.fxToken));
        bind(MetricRegistry.class).toInstance(metricRegistry);
    }
}
