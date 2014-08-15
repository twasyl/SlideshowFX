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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import freemarker.template.*;
import org.vertx.java.core.Handler;
import org.vertx.java.core.VoidHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.platform.Verticle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is responsible for setting the services that will be available for the embedded server.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class SlideshowFXService extends Verticle {
    private static final Logger LOGGER = Logger.getLogger(SlideshowFXService.class.getName());
    private static final String URL = "/slideshowfx";

    @Override
    public void start() {

        if(SlideshowFXServer.getSingleton() != null) {
            final SlideshowFXServer singleton = SlideshowFXServer.getSingleton();

            // Set the RouteMatcher for the HTTP server

            singleton.getHttpServer()
                    .requestHandler(this.buildRouteMatcher())
                    .websocketHandler(this.buildWebSocketHandler())
                    .listen(SlideshowFXServer.getSingleton().getPort(), SlideshowFXServer.getSingleton().getHost());
        }

        this.container.deployVerticle(AttendeeChatService.class.getName());
        this.container.deployVerticle(PresenterChatService.class.getName());
        this.container.deployVerticle(TwitterService.class.getName());
        this.container.deployVerticle(QuizzService.class.getName());
    }

    private RouteMatcher buildRouteMatcher() {
        final Map serverInfo = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_SERVERS);
        final RouteMatcher routeMatcher = new RouteMatcher();

        // Get the main page
        routeMatcher.get(URL, request -> {
            final Map templateTokens = this.vertx.sharedData().getMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setDefaultEncoding("UTF-8");

            configuration.setIncompatibleImprovements(new Version(2, 30, 0));
            configuration.setClassForTemplateLoading(AttendeeChatService.class, "/com/twasyl/slideshowfx/html/");

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_HOST).toString());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), ((Integer) serverInfo.get(SlideshowFXServer.SHARED_DATA_HTTP_SERVER_PORT)).toString());

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("index.html");
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

                // Route that gets the QR code
        .get("/images/chatQRCode.png", request -> {
            request.response().setChunked(true);
            request.response().headers().set("Content-Type", "image/png");

            Buffer buffer = new Buffer(this.generateQRCode(350));
            request.response().write(buffer);
            request.response().end();
        })
        .get(URL.concat("/images/logo.png"), request -> {
            try (final InputStream in = AttendeeChatService.class.getResourceAsStream("/com/twasyl/slideshowfx/images/appicons/64.png")) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = new Buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                request.response().headers().set("Content-Type", "image/png");
                request.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not send check images", e);
            }

        });

        return routeMatcher;
    }

    private Handler<ServerWebSocket> buildWebSocketHandler() {
        final Handler<ServerWebSocket> handler =  serverWebSocket -> {
            if (serverWebSocket.path().equals(URL)) {
                Pump.createPump(serverWebSocket, serverWebSocket).start();

                // Add the textHandlerID to the list of WebSocket clients
                this.vertx.sharedData().getSet(SlideshowFXServer.SHARED_DATA_WEBSOCKET_CLIENTS).add(serverWebSocket.textHandlerID());

                // When the socket is closed, remove it from the list of clients
                serverWebSocket.closeHandler(new VoidHandler() {
                    @Override
                    protected void handle() {
                        SlideshowFXService.this.vertx
                                .sharedData()
                                .getSet(SlideshowFXServer.SHARED_DATA_WEBSOCKET_CLIENTS)
                                .remove(serverWebSocket.textHandlerID());
                    }
                });

                /*
                 * When data are received, get the content which is expected to be a JSON object with two fields:
                 * <ul>
                 *     <li>service: representing the service to call using the EventBus</li>
                 *     <li>data: representing a JSON object containing the data that is expected by the service.</li>
                 * </ul>
                 */
                serverWebSocket.dataHandler(buffer -> {
                    final String bufferString = new String(buffer.getBytes());
                    final JsonObject request = new JsonObject(bufferString);

                    this.vertx.eventBus().send(request.getString("service"), request.getObject("data"), (Message<Object> reply) -> {
                        final JsonObject json = new JsonObject();
                        json.putString("service", request.getString("service"));

                        if(reply.body() instanceof JsonObject) {
                            json.putObject("data", (JsonObject) reply.body());
                        } else if(reply.body() instanceof JsonArray) {
                            json.putArray("data", (JsonArray) reply.body());
                        }

                        serverWebSocket.writeTextFrame(json.encode());
                    });
                });
            } else {
                serverWebSocket.reject();
            }
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
                URL);

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
