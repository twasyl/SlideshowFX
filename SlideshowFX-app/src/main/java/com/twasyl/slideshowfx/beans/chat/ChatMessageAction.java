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
 * Represents the action supported for a {@link com.twasyl.slideshowfx.beans.chat.ChatMessage}. Currently the only action
 * that is supported is marking a ChatMessage as read.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public enum ChatMessageAction {

    MARK_READ("mark-read");

    private final String asString;

    private ChatMessageAction(String action) { this.asString = action; }

    public String getAsString() { return asString; }

    public static ChatMessageAction fromString(String action) {
       if(MARK_READ.getAsString().equals(action)) {
            return MARK_READ;
        } else {
            return null;
        }
    }
}
