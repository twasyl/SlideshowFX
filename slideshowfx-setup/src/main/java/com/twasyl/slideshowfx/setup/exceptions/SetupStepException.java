package com.twasyl.slideshowfx.setup.exceptions;

/**
 * General exception occurring during the setup.
 *
 * @author Thierry Wasylczenko
 * @since SlideshowFX 1.0
 * @version 1.0
 */
public class SetupStepException extends Exception {

    public SetupStepException(String message) {
        super(message);
    }

    public SetupStepException(String message, Throwable cause) {
        super(message, cause);
    }
}
