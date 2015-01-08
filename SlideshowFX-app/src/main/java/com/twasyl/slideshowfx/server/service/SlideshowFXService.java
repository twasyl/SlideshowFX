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

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
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
 * @since SlideshowFX 1.0.0
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

            final Configuration configuration = TemplateProcessor.getHtmlConfiguration();

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
}
