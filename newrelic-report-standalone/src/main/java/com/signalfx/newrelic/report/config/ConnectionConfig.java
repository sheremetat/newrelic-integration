/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.report.config;

public class ConnectionConfig {

    public final String newRelicApiKey;
    public final String newRelicURL;
    public final String fxToken;

    public ConnectionConfig(String newRelicApiKey, String newRelicURL,
                            String fxToken) {
        this.newRelicApiKey = newRelicApiKey;
        this.newRelicURL = newRelicURL;
        this.fxToken = fxToken;
    }
}
