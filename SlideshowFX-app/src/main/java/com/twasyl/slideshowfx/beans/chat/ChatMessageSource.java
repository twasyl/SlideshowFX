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

package com.twasyl.slideshowfx.beans.chat;

/**
 * The source of a {@link com.twasyl.slideshowfx.beans.chat.ChatMessage}.
 * Currently the internal chat of SlideshowFX and Twitter can be the source of a ChatMessage
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public enum ChatMessageSource {

    CHAT("chat"),
    TWITTER("twitter");

    private final String asString;

    private ChatMessageSource(String source) { this.asString = source; }

    public String getAsString() {
        return asString;
    }

    public static ChatMessageSource fromString(String action) {
        if(CHAT.getAsString().equals(action)) {
            return CHAT;
        } else if(TWITTER.getAsString().equals(action)) {
            return TWITTER;
        } else {
            return null;
        }
    }
}
