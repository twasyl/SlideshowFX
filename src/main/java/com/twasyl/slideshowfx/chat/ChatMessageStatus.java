/*
 * Copyright 2014 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.chat;

public enum ChatMessageStatus {
    ANSWERED("answered"),
    NEW("new");

    private String asString;

    private ChatMessageStatus(String asString) { this.asString = asString; }

    public String getAsString() { return asString; }

    public static ChatMessageStatus fromString(String asString) {
        if(ANSWERED.getAsString().equals(asString)) {
            return ANSWERED;
        } else if(NEW.getAsString().equals(asString)) {
            return NEW;
        } else {
            return null;
        }
    }
}
