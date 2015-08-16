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

import com.twasyl.slideshowfx.controls.slideshow.SlideshowPane;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.twasyl.slideshowfx.server.service.IServicesCode.RESPONSE_CODE_MESSAGE_ADDED;

/**
 * This class represents the presenter part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class PresenterChatService extends AbstractSlideshowFXService {

    @Override
    public void start() {
        this.register(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, this.buildMessageHandler());
    }

    @Override
    public void stop() { this.unregisterAll(); }

    private Handler<Message<JsonObject>> buildMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonObject messageContent = message.body();

            if(SlideshowPane.getSingleton() != null) SlideshowPane.getSingleton().publishMessage(messageContent);

            message.reply(this.buildResponse(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, RESPONSE_CODE_MESSAGE_ADDED, ""));
        };

        return handler;
    }
}
