package com.twasyl.lat.exceptions;

public class InvalidTemplateConfigurationException extends RuntimeException {

    public InvalidTemplateConfigurationException() {
        super();
    }

    public InvalidTemplateConfigurationException(String message) {
        super(message);
    }

    public InvalidTemplateConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTemplateConfigurationException(Throwable cause) {
        super(cause);
    }

    protected InvalidTemplateConfigurationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}