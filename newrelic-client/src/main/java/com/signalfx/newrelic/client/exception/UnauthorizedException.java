/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client.exception;

/**
 * Thrown when request cannot be authorized.
 */
public class UnauthorizedException extends Exception {

    public UnauthorizedException(String message) {
        super(message);
    }
}
