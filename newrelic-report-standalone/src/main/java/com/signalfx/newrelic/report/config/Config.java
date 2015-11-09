/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.report.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.signalfx.newrelic.process.info.AppInfo;
import com.signalfx.newrelic.process.info.InfoParser;

/**
 * Config handles retrieval of configurations of the NewRelic SignalFx metric reporting
 *
 * @author 9park
 */
public class Config {

    protected static final Logger log = LoggerFactory.getLogger(Config.class);

    /**
     * Retrieve configurations by looking up in property param first then environment variable.
     *
     * @return ConnectionConfig containing NewRelic URL, Token and SignalFx Token.
     */
    public static ConnectionConfig getConnectionConfig() {
        boolean isValid = true;
        String newRelicApiKey = getPropertyOrEnv("com.signalfx.newrelic.api_key", "NEWRELIC_API_KEY");

        if (StringUtils.isEmpty(newRelicApiKey)) {
            log.error("NewRelic API token not specified.");
            isValid = false;
        }

        String newRelicURL = getPropertyOrEnv("com.signalfx.newrelic.host", "NEWRELIC_HOST");

        if (StringUtils.isEmpty(newRelicURL)) {
            log.error("NewRelic host not specified.");
            isValid = false;
        }
        String fxToken = getPropertyOrEnv("com.signalfx.domains.token", "SIGNALFX_TOKEN");

        if (StringUtils.isEmpty(fxToken)) {
            log.error("SignalFx token not specified.");
            isValid = false;
        }
        if (isValid) {
            return new ConnectionConfig(newRelicApiKey, newRelicURL, fxToken);
        } else {
            return null;
        }
    }

    /**
     * @return path of metrics json configuration for querying NewRelic.
     */
    public static String getMetricJSONPath() {
        String path = getPropertyOrEnv("com.signalfx.newrelic.metrics",
                "SIGNALFX_NEWRELIC_METRICS");
        if (StringUtils.isEmpty(path)) {
            path = "metrics.json";
        }
        return path;
    }

    /**
     * @return interval (in minutes) which the process should retrieve the data range for.
     */
    public static int getInterval() {
        String intervalString = getPropertyOrEnv("com.signalfx.newrelic.interval", "NEWRELIC_INTERVAL");
        int interval;
        if (StringUtils.isEmpty(intervalString)) {
            log.warn("Interval config default to 1");
            interval = 1;
        } else {
            try {
                interval = Integer.parseInt(intervalString);
            } catch (NumberFormatException e) {
                log.warn("Invalid interval config {}, default to 1", intervalString);
                interval = 1;
            }
        }
        if (interval < 1) {
            log.warn("Interval is less than 1 minute minimum, setting to 1 minute");
            interval = 1;
        }
        return interval;
    }

    public static String getPropertyOrEnv(String propertyName, String envName) {
        return System.getProperty(propertyName, System.getenv(envName));
    }

    /**
     * @return list of {@link AppInfo} configurations that the process should query from
     * NewRelic.
     */
    public static AppInfo getSyncConfig() {
        String metricJsonPath = getMetricJSONPath();
        try {
            return InfoParser.parseInfo(new String(Files.readAllBytes(Paths.get(metricJsonPath)),
                    StandardCharsets.UTF_8));
        } catch (NoSuchFileException e) {
            log.error("{} not found", metricJsonPath);
        } catch (IOException e) {
            log.error("error reading {}: {}", metricJsonPath, e.getLocalizedMessage());
        }
        return null;
    }
}
