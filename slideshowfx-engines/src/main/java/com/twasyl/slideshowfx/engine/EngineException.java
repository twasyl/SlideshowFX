package com.twasyl.slideshowfx.engine;

/**
 * This class represents the base exception class for SlideshowFX's engine.
 *
 * @author Thierry Wasylczenko
 */
public class EngineException extends Exception {
    public EngineException() {
        super();
    }

    public EngineException(String message) {
        super(message);
    }

    public EngineException(String message, Throwable cause) {
        super(message, cause);
    }

    public EngineException(Throwable cause) {
        super(cause);
    }

    protected EngineException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
