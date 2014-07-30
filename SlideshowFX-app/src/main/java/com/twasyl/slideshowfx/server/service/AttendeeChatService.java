package com.twasyl.slideshowfx.server.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.beans.chat.ChatMessageSource;
import com.twasyl.slideshowfx.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import freemarker.template.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private final String httpUrl = "/slideshowfx/chat/attendee";
    private Integer realPort;
    private final List<ServerWebSocket> clients = new ArrayList<>();
    private final Map<String, ChatMessage> chatHistory = new HashMap<>();

    @Override
    public void start() {
        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);
        realPort = (Integer) serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_PORT);


        final HttpServer server = this.vertx.createHttpServer();
        server.requestHandler(buildRouteMatcher())
                .websocketHandler(buildWebSocketHandler())
                .listen(realPort,
                        serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());

        this.vertx.eventBus().registerHandler("slideshowfx.chat.attendee.message.add", buildAddMessageHandler())
                .registerHandler("slideshowfx.chat.attendee.message.update", buildUpdateMessageHandler())
                .registerHandler("slideshowfx.chat.attendee.history", buildHistoryMessageHandler());
    }

    @Override
    public void stop() {
        for(ServerWebSocket client : this.clients) {
            client.close();
        }

        this.clients.clear();
        this.chatHistory.clear();
    }

    private RouteMatcher buildRouteMatcher() {
        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);
        final RouteMatcher matcher = new RouteMatcher();

        // Route the attendee chat page
        matcher.get(httpUrl, request -> {
            final Map templateTokens = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setDefaultEncoding("UTF-8");

            configuration.setIncompatibleImprovements(new Version(2, 30, 0));
            configuration.setClassForTemplateLoading(AttendeeChatService.class, "/com/twasyl/slideshowfx/html/");

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), realPort.toString());

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("chat.html");
                template.process(tokenValues, writer);

                writer.flush();

                request.response().setStatusCode(200).setChunked(true).write(writer.toString()).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);

                request.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(Level.WARNING, "Error when processing the chat template", e);
                request.response().setStatusCode(500).end();
            }
        })
        // Route that get the image of an answered message
        .get("/slideshowfx/chat/images/check.png", request -> {
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
        // Route that gets the QR code
        .get("/images/chatQRCode.png", request -> {
            request.response().setChunked(true);
            request.response().headers().set("Content-Type", "image/png");

            Buffer buffer = new Buffer(this.generateQRCode(350));
            request.response().write(buffer);
            request.response().end();
        });

        return matcher;
    }

    private Handler<ServerWebSocket> buildWebSocketHandler() {
        final Handler<ServerWebSocket> handler = serverWebSocket -> {
            if (this.httpUrl.equals(serverWebSocket.path())) {
                Pump.createPump(serverWebSocket, serverWebSocket).start();

                clients.add(serverWebSocket);

                // Send chat history
                for (ChatMessage historyMessage : chatHistory.values()) {
                    serverWebSocket.writeTextFrame(historyMessage.toJSON(serverWebSocket.remoteAddress()));
                }
                serverWebSocket.closeHandler(aVoid -> clients.remove(serverWebSocket));

                serverWebSocket.dataHandler(buffer -> {

                    ChatMessage chatMessage = null;
                    try {
                        chatMessage = ChatMessage.build(new String(buffer.getBytes(), "UTF-8"), serverWebSocket.remoteAddress());
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    chatMessage.setId(System.currentTimeMillis() + "");
                    chatMessage.setStatus(ChatMessageStatus.NEW);
                    chatMessage.setSource(ChatMessageSource.CHAT);
                    chatHistory.put(chatMessage.getId(), chatMessage);

                    this.vertx.eventBus().publish("slideshowfx.chat.presenter.message.add", new JsonObject(chatMessage.toJSON()));

                    for (ServerWebSocket socket : clients) {
                        socket.writeTextFrame(chatMessage.toJSON(socket.remoteAddress()));
                    }
                });
            }
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildUpdateMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            ChatMessage chatMessage = this.chatHistory.get(message.body().getObject("message").getString("id"));

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

                for (ServerWebSocket client : clients) {
                    client.writeTextFrame(chatMessage.toJSON(client.remoteAddress()));
                }
            }
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildAddMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            ChatMessage chatMessage = ChatMessage.build(message.body().encode(), null);
            this.chatHistory.put(chatMessage.getId(), new ChatMessage().build(message.body().encode(), null));

            for (ServerWebSocket client : clients) {
                client.writeTextFrame(chatMessage.toJSON(client.remoteAddress()));
            }
        };

        return handler;
    }

    private Handler<Message<JsonObject>> buildHistoryMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonArray array = new JsonArray();

            for(ChatMessage chatMessage : this.chatHistory.values()) {
                array.addObject(new JsonObject(chatMessage.toJSON()));
            }

            message.reply(array);
        };

        return handler;
    }

    /**
     * Generates the QR code that will be used to access the chat. Data of the QR code are simply the URL of the
     * Chat taking into consideration the <code>Chat.ip</code> and the <code>Chat.port</code>. The URL is of this form:
     * <code>http://ip:port/HTTP_CLIENT_CHAT_PATH</code>. For example: <code>http://127.0.0.1:8080/</code>
     *
     * @param size the size in pixel of the QR code to generate.
     * @return The bytes corresponding to the image of the QR code.
     */
    public byte[] generateQRCode(int size) {
        byte[] qrCode = null;

        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);

        final String qrCodeData = String.format("http://%1$s:%2$s%3$s",
                serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST),
                serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_PORT),
                this.httpUrl);

        final QRCodeWriter qrWriter = new QRCodeWriter();
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            final BitMatrix matrix = qrWriter.encode(qrCodeData, BarcodeFormat.QR_CODE, size, size);

            MatrixToImageWriter.writeToStream(matrix, "png", out);

            out.flush();
            qrCode = out.toByteArray();
        } catch (WriterException | IOException e) {
            LOGGER.log(Level.WARNING, "Can not generate QR Code", e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not close output stream that contains the QR Code", e);
            }
        }

        return qrCode;
    }
}
