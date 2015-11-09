/**
 * Copyright (C) 2015 SignalFx, Inc.
 */
package com.signalfx.newrelic.client.exception;

/**
 * Thrown when generic error occurs.
 */
public class RequestException extends Exception {

    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
