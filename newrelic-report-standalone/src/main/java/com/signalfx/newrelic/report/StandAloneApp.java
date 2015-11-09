/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.report;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.signalfx.newrelic.process.ReportNewRelic;
import com.signalfx.newrelic.report.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.signalfx.newrelic.client.MetricDataRequest;
import com.signalfx.newrelic.report.config.ConnectionConfig;
import com.signalfx.newrelic.process.info.AppInfo;
import com.signalfx.codahale.reporter.SignalFxReporter;

/**
 * StandAloneApp is a stand alone process that reports NewRelic metrics to SignalFx.
 *
 * It use configurations specified in property file/parameters or environment variables.
 *
 * Property Parameters
 *    (Required)
 *    com.signalfx.newrelic.api_key - NewRelic API ke
 *    com.signalfx.newrelic.host - NewRelic host
 *    com.signalfx.domains.token - SignalFx token
 *
 *    (Optional)
 *    com.signalfx.newrelic.metrics - metric configurations filename (default to metrics.json)
 *    com.signalfx.newrelic.interval - time in minutes of metric lookup interval (default to 1 minute)
 *
 * Environment Variables
 *    (Required)
 *    NEWRELIC_API_KEY - NewRelic API ke
 *    NEWRELIC_HOST - NewRelic host
 *    SIGNALFX_TOKEN- SignalFx token
 *
 *    (Optional)
 *    SIGNALFX_NEWRELIC_METRICS - metric configurations filename (default to metrics.json)
 *    NEWRELIC_INTERVAL - time in minutes of metric lookup interval (default to 1 minute)
 *
 * It also uses metric configuration json file to perform query of metrics from NewRelic and
 * do the mapping to metric names/dimensions in SignalFx.
 *
 * @author 9park
 */
public class StandAloneApp {

    protected static final Logger log = LoggerFactory.getLogger(StandAloneApp.class);
    protected static final SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");

    public static final int MINUTE_MILLIS = 60 * 1000;

    public static void main(String[] args) {
        ConnectionConfig connectionConfig = Config.getConnectionConfig();
        AppInfo appInfo = Config.getSyncConfig();
        if (appInfo == null || connectionConfig == null) {
            log.info("No apps, no connection config. Stop Application...");
            return;
        }
        log.debug(appInfo.toString());


        int interval = Config.getInterval();

        log.info("Starting NewRelic sync of all rule(s) at {} minute(s) interval",
                interval);

        MetricRegistry metricRegistry = new MetricRegistry();
        SignalFxReporter signalFxReporter =
                new SignalFxReporter.Builder(metricRegistry, connectionConfig.fxToken).build();
        signalFxReporter.start(1, TimeUnit.SECONDS);

        Injector injector =
                Guice.createInjector(new NewRelicReportModule(connectionConfig, metricRegistry));

        ReportNewRelic syncNewRelic = injector.getInstance(ReportNewRelic.class);

        int intervalMillis = interval * MINUTE_MILLIS;
        long lastStart = System.currentTimeMillis();
        while (true) {
            long timeStart = System.currentTimeMillis();

            long timeDiff = timeStart - lastStart;
            long range = timeDiff / MINUTE_MILLIS + (timeDiff % MINUTE_MILLIS > 0 ? 1 : 0) + 1;
            lastStart = timeStart;
            log.trace("Starting at {} and querying for range {}",
                    format.format(new Date(timeStart)), range);

            // Perform the actual stuff.
            syncNewRelic.perform(appInfo, MetricDataRequest.TimeParams.beforeNow(range));

            long timeEnd = System.currentTimeMillis();
            long sleepTime;
            long timeTaken = timeEnd - timeStart;

            if (timeTaken > intervalMillis) {
                log.warn("Took {} to process which is more than {} interval", timeTaken,
                        intervalMillis);

                // Perform the next one at the next minute
                sleepTime = MINUTE_MILLIS - timeTaken % MINUTE_MILLIS;
            } else {
                sleepTime = intervalMillis - timeTaken;
            }

            log.trace("Took {}, sleeping for {}", timeTaken, sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                log.error("Sleep got interrupted");
                return;
            }
        }
    }
}
