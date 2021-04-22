package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.SlideshowFXServer.*;
import static com.twasyl.slideshowfx.server.service.IServicesCode.*;
import static java.util.logging.Level.WARNING;

/**
 * This class represents the attendee part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class AttendeeChatService extends AbstractSlideshowFXService {
    private static final Logger LOGGER = Logger.getLogger(AttendeeChatService.class.getName());

    private final Map<String, ChatMessage> chatHistory = new HashMap<>();

    @Override
    public void start() {
        this.updateRouteMatcher();

        this.register(SERVICE_CHAT_ATTENDEE_MESSAGE_ADD, buildAddMessageHandler())
                .register(SERVICE_CHAT_ATTENDEE_MESSAGE_UPDATE, buildUpdateMessageHandler())
                .register(SERVICE_CHAT_ATTENDEE_HISTORY, buildHistoryMessageHandler());
    }

    @Override
    public void stop() {
        this.unregisterAll();
        this.chatHistory.clear();
    }

    private void updateRouteMatcher() {
        final SlideshowFXServer singleton = SlideshowFXServer.getSingleton();
        final Router router = singleton.getRouter();

        // Get the JavaScript resources
        router.get("/slideshowfx/chat/js/chatService.js").handler(request -> {
            final LocalMap templateTokens = this.vertx.sharedData().getLocalMap(SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getJsConfiguration(AttendeeChatService.class);

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SHARED_DATA_SERVER_HOST_TOKEN).toString(), singleton.getHost());
            tokenValues.put(templateTokens.get(SHARED_DATA_SERVER_PORT_TOKEN).toString(), singleton.getPort() + "");

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("chatService.js");
                template.process(tokenValues, writer);

                writer.flush();

                request.response().putHeader("Content-Type", "application/javascript").setStatusCode(200).setChunked(true).end(Buffer.buffer(writer.toString()));
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error when a client tried to access the chat", e);

                request.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(WARNING, "Error when processing the chat template", e);
                request.response().setStatusCode(500).end();
            }
        });
    }

    private Handler<Message<JsonObject>> buildUpdateMessageHandler() {
        return message -> {
            final String messageId = message.body().getJsonObject(JSON_KEY_MESSAGE).getString(JSON_KEY_MESSAGE_ID);
            final ChatMessage chatMessage = this.chatHistory.get(messageId);

            int responseCode;
            Object responseContent;

            if (chatMessage != null) {
                JsonArray fields = message.body().getJsonArray(JSON_KEY_FIELDS);
                fields.forEach(value -> {
                    if (JSON_KEY_FIELD_STATUS.equals(value)) {
                        chatMessage.setStatus(ChatMessageStatus.fromString(message.body().getJsonObject(JSON_KEY_MESSAGE).getString(JSON_KEY_MESSAGE_STATUS)));
                    } else if (JSON_KEY_FIELD_ACTION.equals(value)) {
                        final String action = message.body().getJsonObject(JSON_KEY_MESSAGE).getString(JSON_KEY_MESSAGE_ACTION);
                        chatMessage.setAction("null".equals(action) ? null : ChatMessageAction.fromString(action));
                    }
                });

                this.chatHistory.put(chatMessage.getId(), chatMessage);

                final JsonObject object = this.buildResponse(SERVICE_CHAT_ATTENDEE_MESSAGE_UPDATE, RESPONSE_CODE_MESSAGE_UPDATED, chatMessage.toJSON());
                this.sendResponseToWebSocketClients(object);

                responseCode = RESPONSE_CODE_MESSAGE_UPDATED;
                responseContent = chatMessage.toJSON();
            } else {
                responseCode = RESPONSE_CODE_MESSAGE_NOT_FOUND;
                responseContent = new JsonObject().put(JSON_KEY_MESSAGE_ID, messageId);
            }

            message.reply(this.buildResponse(SERVICE_CHAT_ATTENDEE_MESSAGE_UPDATE, responseCode, responseContent));
        };
    }

    private Handler<Message<JsonObject>> buildAddMessageHandler() {
        return message -> {
            final ChatMessage chatMessage = ChatMessage.build(message.body().encode(), null);
            chatMessage.setId("msg-" + System.currentTimeMillis());
            this.chatHistory.put(chatMessage.getId(), chatMessage);

            final JsonObject object = this.buildResponse(SERVICE_CHAT_ATTENDEE_MESSAGE_ADD, RESPONSE_CODE_MESSAGE_ADDED, chatMessage.toJSON());

            final String origin = message.body().getString(JSON_KEY_ORIGIN);
            this.sendResponseToWebSocketClients(object, origin);

            this.vertx.eventBus().send(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, chatMessage.toJSON());
            message.reply(object);
        };
    }

    private Handler<Message<JsonObject>> buildHistoryMessageHandler() {
        return message -> {
            final JsonArray array = new JsonArray();

            for (ChatMessage chatMessage : this.chatHistory.values()) {
                array.add(chatMessage.toJSON());
            }

            message.reply(this.buildResponse(SERVICE_CHAT_ATTENDEE_HISTORY, RESPONSE_CODE_OK, array));
        };
    }
}
