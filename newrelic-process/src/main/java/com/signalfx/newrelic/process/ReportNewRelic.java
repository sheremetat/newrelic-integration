/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.signalfx.newrelic.client.MetricDataRequest;
import com.signalfx.newrelic.client.exception.RequestException;
import com.signalfx.newrelic.client.exception.UnauthorizedException;
import com.signalfx.newrelic.client.model.MetricData;
import com.signalfx.newrelic.process.info.AppInfo;
import com.signalfx.newrelic.process.model.MetricTimeSeries;
import com.signalfx.newrelic.process.processor.Processor;
import com.signalfx.newrelic.process.reporter.Reporter;
import com.signalfx.newrelic.process.status.StatusType;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers;

/**
 * ReportNewRelic performs reporting of NewRelic metrics to SignalFx
 *
 * @author 9park
 */
public class ReportNewRelic {

    protected static final Logger log = LoggerFactory.getLogger(ReportNewRelic.class);

    private final MetricDataRequest dataRequest;
    private final Processor processor;
    private final Reporter reporter;

    private final Counter counterDataPointsReported;
    private final Counter counterMtsReported;
    private final Counter counterMtsEmpty;
    private final Counter counterNewRelicRequestFailure;

    @Inject
    public ReportNewRelic(MetricDataRequest metricDataRequest, Processor processor, Reporter reporter,
                          MetricRegistry metricRegistry) {
        this.dataRequest = metricDataRequest;
        this.processor = processor;
        this.reporter = reporter;

        counterDataPointsReported = metricRegistry.counter(StatusType.dataPointsReported.name());
        counterMtsReported = metricRegistry.counter(StatusType.mtsReported.name());
        counterMtsEmpty = metricRegistry.counter(StatusType.mtsEmpty.name());
        counterNewRelicRequestFailure = metricRegistry.counter(StatusType.newRelicRequestFailure.name());
    }

    /**
     * Perform reading and reporting of NewRelic metrics to SignalFx
     *
     * @param timeParams Time paracounters to query metrics from NewRelic.
     */
    public void perform(AppInfo app, MetricDataRequest.TimeParams timeParams) {
        List<SignalFxProtocolBuffers.DataPoint> dataPoints = new LinkedList<>();
        for (String domain : app.domains) {
            dataRequest.setAppName(domain);
            dataRequest.setTimeParams(timeParams);
            List<MetricData> metricDataList;
            try {
                metricDataList = dataRequest.get();
            } catch (RequestException e) {
                // too bad
                log.error("Metric query failure for \"{}\"", domain);
                counterNewRelicRequestFailure.inc();
                continue;
            } catch (UnauthorizedException e) {
                log.error("NewRelic authentication failed");
                return;
            }
            if (metricDataList != null && metricDataList.size() > 0) {
                log.info("Metrics size {}", metricDataList.size());
                log.info(metricDataList.toString());
                for (MetricData metricData : metricDataList) {
                    MetricTimeSeries mts =
                            app.getMetricTimeSeries(metricData.metricName, metricData.dimensions);
                    List<SignalFxProtocolBuffers.DataPoint> mtsDataPoints = processor
                            .process(mts, metricData.metricValues);
                    dataPoints.addAll(mtsDataPoints);
                    if (!mtsDataPoints.isEmpty()) {
                        counterMtsReported.inc();
                    } else {
                        counterMtsEmpty.inc();
                    }
                }
            } else {
                // no metrics found, something is wrong with selection
                log.warn("No metric found ");
            }
        }
        if (!dataPoints.isEmpty()) {
            try {
                reporter.report(dataPoints);
                counterDataPointsReported.inc(dataPoints.size());
            } catch (Reporter.ReportException e) {
                log.error("There were errors reporting metric");
            }
        }
    }
}
