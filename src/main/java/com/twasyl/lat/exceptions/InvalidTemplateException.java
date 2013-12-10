package com.twasyl.lat.exceptions;

public class InvalidTemplateException extends Exception {

    public InvalidTemplateException() {
    }

    public InvalidTemplateException(String message) {
        super(message);
    }

    public InvalidTemplateException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTemplateException(Throwable cause) {
        super(cause);
    }

    public InvalidTemplateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
