/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
     * The status code indicating the quizz has been started successfully.
     */
    int RESPONSE_CODE_QUIZZ_STARTED = 203;

    /**
     * The status code indicating the quizz has been stopped successfully.
     */
    int RESPONSE_CODE_QUIZZ_STOPPED = 204;

    /**
     * The status code indicating the current quizz has been retrieved successfully.
     */
    int RESPONSE_CODE_QUIZZ_RETRIEVED = 205;

    /**
     * The status code indicating an error occurred.
     */
    int RESPONSE_CODE_ERROR = 400;

    /**
     * The status code indicating that a chat message has not been found.
     */
    int RESPONSE_CODE_MESSAGE_NOT_FOUND = 404;

    /**
     * The status code indicating tno quizz is currently active.
     */
    int RESPONSE_CODE_QUIZZ_NOT_ACTIVE = 404;
}
