package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import freemarker.template.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the presneter part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class PresenterChatService extends Verticle {
    private static final Logger LOGGER = Logger.getLogger(PresenterChatService.class.getName());

    private Integer realPort;
    private ServerWebSocket presenter;
    private final String httpUrl = "/slideshowfx/chat/presenter";

    @Override
    public void start() {
        Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);

        realPort = (Integer) serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_PORT) + 1;


        this.vertx.createHttpServer()
                .requestHandler(buildRouteMatcher())
                .websocketHandler(buildWebSocketHandler())
                .listen(realPort,
                        serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());

        this.vertx.eventBus().registerHandler("slideshowfx.chat.presenter.message.add", buildMessageHandler());
    }

    @Override
    public void stop() {
        if(presenter != null) {
            presenter.close();
            presenter = null;
        }
    }

    private RouteMatcher buildRouteMatcher() {
        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);
        final RouteMatcher routeMatcher = new RouteMatcher();

        // Route the attendee chat page
        routeMatcher.get(httpUrl, request -> {
            final Map templateTokens = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setDefaultEncoding("UTF-8");
            configuration.setIncompatibleImprovements(new Version(2, 30, 0));
            configuration.setClassForTemplateLoading(PresenterChatService.class, "/com/twasyl/slideshowfx/html/");

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), realPort.toString());

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("presenter.html");
                template.process(tokenValues, writer);

                writer.flush();

                request.response().setStatusCode(200).setChunked(true).write(writer.toString()).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when the presenter tried to access the chat", e);

                request.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(Level.WARNING, "Error when processing the presenter chat template", e);
                request.response().setStatusCode(500).end();
            }
        });

        return routeMatcher;
    }

    private Handler<ServerWebSocket> buildWebSocketHandler() {
        final Handler<ServerWebSocket> handler = serverWebSocket -> {
            if (this.httpUrl.equals(serverWebSocket.path())) {
                presenter = serverWebSocket;

                presenter.dataHandler( buffer -> {
                    ChatMessage chatMessage = null;
                    try {
                        chatMessage = ChatMessage.build(new String(buffer.getBytes(), "UTF-8"), serverWebSocket.remoteAddress());
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.log(Level.SEVERE, "Can not build the chat message", e);
                    }

                    if (chatMessage.getAction() == ChatMessageAction.MARK_READ) {
                        chatMessage.setStatus(ChatMessageStatus.ANSWERED);
                        chatMessage.setAction(null);
                    }

                    final JsonObject request = new JsonObject(chatMessage.toJSON());
                    request.putArray("fields", new JsonArray().addString("status").addString("action"));

                    this.vertx.eventBus().publish("slideshowfx.chat.attendee.message.update", request);
                });

                this.vertx.eventBus().send("slideshowfx.chat.attendee.history", new JsonObject(), (Message<JsonArray> message) -> {
                    for(Object obj : message.body()) {
                       presenter.writeTextFrame(((JsonObject) obj).encode());
                    }
                });
            }
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonObject messageContent = message.body();

            if(presenter != null) presenter.writeTextFrame(messageContent.encodePrettily());
        };

        return handler;
    }
}
