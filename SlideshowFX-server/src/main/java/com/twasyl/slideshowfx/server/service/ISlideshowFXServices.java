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

package com.twasyl.slideshowfx.server.service;

/**
 * This interface lists the base services provided by SlideshowFX for the embedded server.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public interface ISlideshowFXServices {
    String SERVICE_CHAT_ATTENDEE_MESSAGE_ADD = "slideshowfx.chat.attendee.message.add";
    String SERVICE_CHAT_ATTENDEE_MESSAGE_UPDATE = "slideshowfx.chat.attendee.message.update";
    String SERVICE_CHAT_ATTENDEE_HISTORY = "slideshowfx.chat.attendee.history";

    String SERVICE_CHAT_PRESENTER_MESSAGE_ADD = "slideshowfx.chat.presenter.message.add";

    String SERVICE_QUIZ_START = "slideshowfx.quiz.start";
    String SERVICE_QUIZ_STOP = "slideshowfx.quiz.stop";
    String SERVICE_QUIZ_CURRENT = "slideshowfx.quiz.current";
}
