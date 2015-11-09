/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.process.status;

/**
 * List of health data of reporting process.
 *
 * @author 9park
 */
public enum StatusType {
    mtsReported,
    mtsEmpty,
    dataPointsReported,
    newRelicRequestFailure
}
