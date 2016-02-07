package com.twasyl.slideshowfx.engine.presentation;

public class PresentationException extends Exception {

    public PresentationException() {
    }

    public PresentationException(String message) {
        super(message);
    }

    public PresentationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PresentationException(Throwable cause) {
        super(cause);
    }

    public PresentationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
