package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.utils.ResourceHelper;
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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.service.IServicesCode.*;
/**
 * This class represents the attendee part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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

        // Route that get the image of an answered message
        final String FONT_AWESOME_PREFIX = "/slideshowfx/font-awesome/";
        router.get(FONT_AWESOME_PREFIX.concat("*")).handler(routingContext -> {
            final String file = routingContext.request().path().substring(FONT_AWESOME_PREFIX.length());

            try (final InputStream in = ResourceHelper.getInputStream("/com/twasyl/slideshowfx/webapp/font-awesome/4.5.0/".concat(file))) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = Buffer.buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                routingContext.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not send the font awesome css", e);
            }
        });
        // Get the JavaScript resources
        router.get("/slideshowfx/chat/js/chatService.js").handler(request -> {
            final LocalMap templateTokens = this.vertx.sharedData().getLocalMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getJsConfiguration();

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), singleton.getHost());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), singleton.getPort() + "");

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("chatService.js");
                template.process(tokenValues, writer);

                writer.flush();

                request.response().putHeader("Content-Type", "application/javascript").setStatusCode(200).setChunked(true).write(Buffer.buffer(writer.toString())).end();
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
            final String messageId = message.body().getJsonObject(JSON_KEY_MESSAGE).getString(JSON_KEY_MESSAGE_ID);
            final ChatMessage chatMessage = this.chatHistory.get(messageId);

            int responseCode;
            Object responseContent;

            if (chatMessage != null) {
                JsonArray fields = message.body().getJsonArray(JSON_KEY_FIELDS);
                fields.forEach(value -> {
                    if(JSON_KEY_FIELD_STATUS.equals(value)) {
                        chatMessage.setStatus(ChatMessageStatus.fromString(message.body().getJsonObject(JSON_KEY_MESSAGE).getString(JSON_KEY_MESSAGE_STATUS)));
                    } else if(JSON_KEY_FIELD_ACTION.equals(value)) {
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

        return handler;
    }

    private Handler<Message<JsonObject>> buildAddMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final ChatMessage chatMessage = ChatMessage.build(message.body().encode(), null);
            chatMessage.setId("msg-" + System.currentTimeMillis());
            this.chatHistory.put(chatMessage.getId(), chatMessage);

            final JsonObject object = this.buildResponse(SERVICE_CHAT_ATTENDEE_MESSAGE_ADD, RESPONSE_CODE_MESSAGE_ADDED, chatMessage.toJSON());

            final String origin = message.body().getString(JSON_KEY_ORIGIN);
            this.sendResponseToWebSocketClients(object, origin);

            this.vertx.eventBus().send(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, chatMessage.toJSON());
            message.reply(object);
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildHistoryMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonArray array = new JsonArray();

            for(ChatMessage chatMessage : this.chatHistory.values()) {
                array.add(chatMessage.toJSON());
            }

            message.reply(this.buildResponse(SERVICE_CHAT_ATTENDEE_HISTORY, RESPONSE_CODE_OK, array));
        };

        return handler;
    }
}
