/*
 * Copyright 2016 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.server.bus;

/**
 * @author Thierry Wasylczenko
 * @since SlideshowFX
 */
public interface Actor {

    /**
     * Tests if the given message is supported by this actor. A message can be considered as supported if it is not
     * {@code null} or if it is an instance of a given class. The criteria of supported message is determined by the
     * implementing class.
     *
     * @param message The message to test if it is supported or not.
     * @return {@code true} if te message is considered as supported, {@code false} otherwise.
     */
    boolean supportsMessage(final Object message);

    /**
     * Performs an action when the message is sent. This method is only called by the {@link EventBus} if the message is
     * considered as supported by this actor, as defined by the {@link #supportsMessage(Object)} method. This avoids
     * implementations of this method to take care of the message support.
     *
     * @param message The message sent.
     */
    void onMessage(final Object message);
}
