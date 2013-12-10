package com.twasyl.slideshowfx.exceptions;

public class InvalidTemplateConfigurationException extends Exception {

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
