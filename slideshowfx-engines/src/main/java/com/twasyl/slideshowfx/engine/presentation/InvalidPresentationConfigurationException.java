package com.twasyl.slideshowfx.engine.presentation;

public class InvalidPresentationConfigurationException extends Exception {

    public InvalidPresentationConfigurationException() {
    }

    public InvalidPresentationConfigurationException(String message) {
        super(message);
    }

    public InvalidPresentationConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidPresentationConfigurationException(Throwable cause) {
        super(cause);
    }

    public InvalidPresentationConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
