package com.twasyl.slideshowfx.hosting.connector.exceptions;

import com.twasyl.slideshowfx.utils.beans.Pair;

/**
 * Base exception class to define errors occurring when dealing with an hosting connector. This class also defines
 * error codes to identify the error.
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public class HostingConnectorException extends Exception {
    public static final short UNKNOWN_ERROR = 1;
    public static final short NOT_AUTHENTICATED = 2;
    public static final short AUTHENTICATION_FAILURE = 3;
    public static final short MISSING_CONFIGURATION = 4;

    private final short errorCode;

    public HostingConnectorException(final short errorCode) {
        this.errorCode = errorCode;
    }

    public HostingConnectorException(final short errorCode, final String message, final Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Get the error code for this exception.
     * @return The error code of this exception.
     */
    public short getErrorCode() { return errorCode; }

    /**
     * Returns the title and the message for this exception according its error code.
     * @return A {@link Pair} containing the title as key, and the message as value.
     */
    public Pair<String, String> getTitleAndMessage() {
        final Pair<String, String> pair = new Pair<>();

        switch(this.errorCode) {
            case UNKNOWN_ERROR:
                pair.setKey("Unknown error");
                pair.setValue("An error occurred when interacting with the service");
                break;
            case NOT_AUTHENTICATED:
                pair.setKey("Not authenticated");
                pair.setValue("Authentication is required");
                break;
            case AUTHENTICATION_FAILURE:
                pair.setKey("Authentication failed");
                pair.setValue("The authentication failed");
                break;
            case MISSING_CONFIGURATION:
                pair.setKey("Missing configuration");
                pair.setValue("A configuration is missing in order to interact with the service");
        }
        return pair;
    }
}
