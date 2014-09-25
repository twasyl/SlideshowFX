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

package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import freemarker.template.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the attendee part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class AttendeeChatService extends Verticle {
    private static final Logger LOGGER = Logger.getLogger(AttendeeChatService.class.getName());

    private final Map<String, ChatMessage> chatHistory = new HashMap<>();

    @Override
    public void start() {
        this.updateRouteMatcher();

        this.vertx.eventBus().registerHandler("slideshowfx.chat.attendee.message.add", buildAddMessageHandler())
                .registerHandler("slideshowfx.chat.attendee.message.update", buildUpdateMessageHandler())
                .registerHandler("slideshowfx.chat.attendee.history", buildHistoryMessageHandler());
    }

    @Override
    public void stop() {
        this.chatHistory.clear();
    }

    private void updateRouteMatcher() {
        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);
        final SlideshowFXServer singleton = SlideshowFXServer.getSingleton();
        final RouteMatcher routeMatcher = (RouteMatcher) singleton.getHttpServer().requestHandler();

        // Route that get the image of an answered message
        routeMatcher.get("/slideshowfx/chat/images/check.png", request -> {
            try (final InputStream in = AttendeeChatService.class.getResourceAsStream("/com/twasyl/slideshowfx/html/images/check.png")) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = new Buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                request.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not send check images", e);
            }
        })
        // Get the JavaScript resources
        .get("/slideshowfx/chat/js/chatService.js", request -> {
            final Map templateTokens = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setDefaultEncoding("UTF-8");

            configuration.setIncompatibleImprovements(new Version(2, 30, 0));
            configuration.setClassForTemplateLoading(AttendeeChatService.class, "/com/twasyl/slideshowfx/js/");

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), ((Integer) serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_PORT)).toString());

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("chatService.js");
                template.process(tokenValues, writer);

                writer.flush();

                request.response().putHeader("Content-Type", "text/javascript").setStatusCode(200).setChunked(true).write(writer.toString()).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);

                request.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(Level.WARNING, "Error when processing the chat template", e);
                request.response().setStatusCode(500).end();
            }
        });
    }

    private Handler<Message<JsonObject>> buildUpdateMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final ChatMessage chatMessage = this.chatHistory.get(message.body().getObject("message").getString("id"));

            if (chatMessage != null) {
                JsonArray fields = message.body().getArray("fields");
                fields.forEach(value -> {
                    if("status".equals(value)) {
                        chatMessage.setStatus(ChatMessageStatus.fromString(message.body().getObject("message").getString("status")));
                    } else if("action".equals(value)) {
                        final String action = message.body().getObject("message").getString("action");
                        chatMessage.setAction("null".equals(action) ? null : ChatMessageAction.fromString(action));
                    }
                });

                this.chatHistory.put(chatMessage.getId(), chatMessage);

                final JsonObject object = new JsonObject();
                object.putString("service", "slideshowfx.chat.attendee.message.update");
                object.putObject("data", chatMessage.toJSON());

                for(Object textHandlerId : this.vertx.sharedData().getSet(SlideshowFXServer.SHARED_DATA_WEBSOCKET_CLIENTS)) {
                    this.vertx.eventBus().send((String) textHandlerId, object.encode());
                }
            }

            message.reply();
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildAddMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final ChatMessage chatMessage = ChatMessage.build(message.body().encode(), null);
            chatMessage.setId("msg-" + System.currentTimeMillis());
            this.chatHistory.put(chatMessage.getId(), chatMessage);

            final JsonObject object = new JsonObject();
            object.putString("service", "slideshowfx.chat.attendee.message.add");
            object.putObject("data", chatMessage.toJSON());

            for(Object textHandlerId : this.vertx.sharedData().getSet(SlideshowFXServer.SHARED_DATA_WEBSOCKET_CLIENTS)) {
                this.vertx.eventBus().send((String) textHandlerId, object.encode());
            }

            this.vertx.eventBus().send("slideshowfx.chat.presenter.message.add", chatMessage.toJSON());

            message.reply();
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildHistoryMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonArray array = new JsonArray();

            for(ChatMessage chatMessage : this.chatHistory.values()) {
                array.addObject(chatMessage.toJSON());
            }

            message.reply(array);
        };

        return handler;
    }
}
