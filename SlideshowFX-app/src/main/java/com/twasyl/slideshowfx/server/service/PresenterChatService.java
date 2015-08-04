/*
 * Copyright 2015 Thierry Wasylczenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.controls.slideshow.SlideshowPane;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.logging.Logger;

/**
 * This class represents the presenter part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class PresenterChatService extends Verticle {
    private static final Logger LOGGER = Logger.getLogger(PresenterChatService.class.getName());

    private final String handlerAddress = "slideshowfx.chat.presenter.message.add";
    private Handler<Message<JsonObject>> messagehandler;

    @Override
    public void start() {
        this.messagehandler = buildMessageHandler();
        this.vertx.eventBus().registerHandler(this.handlerAddress, this.messagehandler);
    }

    @Override
    public void stop() {
        this.vertx.eventBus().unregisterHandler(this.handlerAddress, this.messagehandler);
    }

    private Handler<Message<JsonObject>> buildMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonObject messageContent = message.body();

            if(SlideshowPane.getSingleton() != null) SlideshowPane.getSingleton().publishMessage(messageContent);

            message.reply();
        };

        return handler;
    }
}
