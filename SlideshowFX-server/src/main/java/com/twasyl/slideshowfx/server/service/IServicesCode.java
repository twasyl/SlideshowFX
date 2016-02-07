package com.twasyl.slideshowfx.server.service;

/**
 * <p>This interfaces lists all common response codes returned by a {@link AbstractSlideshowFXService}.</p>
 *
 * @author Thierry Wasylczenko
 * @version 1.0.0
 * @since SlideshowFX 1.0.0
 */
public interface IServicesCode {
    /**
     * The status code indicating the service went normally.
     */
    int RESPONSE_CODE_OK = 200;

    /**
     * The status code indicating the chat has been added successfully.
     */
    int RESPONSE_CODE_MESSAGE_ADDED = 201;

    /**
     * The status code indicating the chat message has been updated successfully.
     */
    int RESPONSE_CODE_MESSAGE_UPDATED = 202;

    /**
     * The status code indicating the quiz has been started successfully.
     */
    int RESPONSE_CODE_QUIZ_STARTED = 203;

    /**
     * The status code indicating the quiz has been stopped successfully.
     */
    int RESPONSE_CODE_QUIZ_STOPPED = 204;

    /**
     * The status code indicating the current quiz has been retrieved successfully.
     */
    int RESPONSE_CODE_QUIZ_RETRIEVED = 205;

    /**
     * The status code indicating an error occurred.
     */
    int RESPONSE_CODE_ERROR = 400;

    /**
     * The status code indicating that a chat message has not been found.
     */
    int RESPONSE_CODE_MESSAGE_NOT_FOUND = 404;

    /**
     * The status code indicating tno quiz is currently active.
     */
    int RESPONSE_CODE_QUIZ_NOT_ACTIVE = 404;
}
