package com.twasyl.slideshowfx.global.configuration;

/**
 * Exception class when working with the SlideshowFX context file.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 2.0
 */
public class ContextFileException extends Exception {
    public ContextFileException() {
    }

    public ContextFileException(String message) {
        super(message);
    }

    public ContextFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContextFileException(Throwable cause) {
        super(cause);
    }

    public ContextFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
