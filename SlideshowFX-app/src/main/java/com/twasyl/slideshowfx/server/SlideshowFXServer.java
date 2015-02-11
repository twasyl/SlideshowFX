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

package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.SlideshowFXService;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.JsonElement;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents an embedded server which is provided in SlideshowFX. The server currently deploys four services
 * that provides feature for the application:
 * <ul>
 *     <li>{@link com.twasyl.slideshowfx.server.service.AttendeeChatService} : allows the audience to access a chat to communicate with the presenter;</li>
 *     <li>{@link com.twasyl.slideshowfx.server.service.PresenterChatService} : allows the presenter to display the chat during the presentation;</li>
 *     <li>{@link com.twasyl.slideshowfx.server.service.TwitterService} : allows to listen the Twitter stream for a particular hashtag in order to display tweets in
 *     the chat;</li>
 *     <li>{@link com.twasyl.slideshowfx.server.service.QuizzService} : allows to publish quizz so the audience can answer to them.</li>
 * </ul>
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowFXServer {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFXServer.class.getName());

    private static SlideshowFXServer singleton;

    private PlatformManager platformManager;

    private HttpServer httpServer;

    /**
     * The shared data that contains the TextHandlerID of WebSocket clients.
     */
    public static final String SHARED_DATA_WEBSOCKET_CLIENTS = "websocket.clients";

    /**
     * The shared data that contains all tokens that can be found in templates.
     */
    public static final String SHARED_DATA_TEMPLATE_TOKENS = "template.tokens";

    /**
     * The shared data that contains all servers.
     */
    public static final String SHARED_DATA_SERVERS = "servers";

    /**
     * The host the HTTP server is listening on.
     */
    public static final String SHARED_DATA_HTTP_SERVER_HOST = "server.http.host";

    /**
     * The port the HTTP server is listening on.
     */
    public static final String SHARED_DATA_HTTP_SERVER_PORT = "server.http.port";

    /**
     * The shared data containing Twitter information
     */
    public static final String SHARED_DATA_TWITTER = "twitter";

    /**
     * The Twitter hashtag used for the streaming.
     */
    public static final String SHARED_DATA_TWITTER_HASHTAG = "twitter.hashtag";

    /**
     * The name of the key for retrieving the host the server is listening on and stored in {@link #SHARED_DATA_TEMPLATE_TOKENS}
     */
    public static final String SHARED_DATA_SERVER_HOST_TOKEN = "template.tokens.server.host";

    /**
     * The name of the key for retrieving the port the server is listening on and stored in {@link #SHARED_DATA_TEMPLATE_TOKENS}
     */
    public static final String SHARED_DATA_SERVER_PORT_TOKEN = "template.tokens.server.port";

    private String host;
    private int port;

    public SlideshowFXServer(String host, int port, String twitterHashtag) {
        singleton = this;

        this.host = host;
        this.port = port;

        this.platformManager = PlatformLocator.factory.createPlatformManager();

        this.httpServer = this.platformManager.vertx().createHttpServer();

        Map serversInfo = this.platformManager.vertx().sharedData().getMap(SHARED_DATA_SERVERS);
        serversInfo.put(SHARED_DATA_HTTP_SERVER_HOST, host);
        serversInfo.put(SHARED_DATA_HTTP_SERVER_PORT, port);

        /*
         * Add some useful resources that can be shared by multiple Verticles.
         * One of them is a map named "template.tokens" which contains tokens that can be found in a template.
         */
        Map templateTokens = this.platformManager.vertx().sharedData().getMap(SHARED_DATA_TEMPLATE_TOKENS);
        templateTokens.put(SHARED_DATA_SERVER_HOST_TOKEN, "slideshowfx_server_ip");
        templateTokens.put(SHARED_DATA_SERVER_PORT_TOKEN, "slideshowfx_server_port");

        /*
         * Add the Twitter information
         */
        Map twitter = this.platformManager.vertx().sharedData().getMap(SHARED_DATA_TWITTER);
        twitter.put(SHARED_DATA_TWITTER_HASHTAG, twitterHashtag);

        deploy(SlideshowFXService.class);
    }

    /**
     * Deploy a {@link org.vertx.java.platform.Verticle} identified by the given class.
     *
     * @param clazz The class of the Verticle to deploy.
     */
    public void deploy(Class clazz) {
        URL[] classpath = new URL[0];

        this.platformManager.deployVerticle(clazz.getName(), null, classpath, 1, null,  result -> {
            if (result.succeeded()) {
                LOGGER.log(Level.FINE, "Verticle has been deployed successfully. Result: " + result.result());
            } else if (result.failed()) {
                LOGGER.log(Level.WARNING, "Verticle hasn't been deployed properly. Result: " + result.result(), result.cause());
            }
        });
    }

    /**
     * Get the host of the server.
     * @return The host of the server.
     */
    public String getHost() { return host; }

    /**
     * Get the port of the server
     * @return The port of the server.
     */
    public int getPort() { return port; }

    /**
     * Get the HTTP server that has been created with this server.
     * @return The HTTP server created by this server.
     */
    public HttpServer getHttpServer() { return httpServer; }

    /**
     * Stops the embedded server.
     */
    public void stop() {
        this.platformManager.undeployAll(result -> {
            if(result.succeeded()) {
                LOGGER.log(Level.INFO, "All services for embedded server undeployed successfully");
            } else if(result.failed()) {
                LOGGER.log(Level.WARNING, "All services for embedded server haven't been undeployed properly", result.cause());
            }
        });

        this.platformManager.stop();
        this.platformManager = null;
        singleton = null;
    }

    /**
     * Call a service using the EventBus of Vert.x. The request must be a JSON object with a field named {@code service}
     * representing the service to call, and a JSON object named {@code data} containing the data the service will
     * consume.
     * @param request The JSON object corresponding to the service to call.
     * @return The response corresponding to the request.
     * @throws java.lang.IllegalArgumentException If the request is invalid.
     */
    public JsonElement callService(String request) throws IllegalArgumentException {
        JsonElement response = null;

        try {
            final JsonObject jsonRequest = new JsonObject(request);

            final String service = jsonRequest.getString("service");
            final JsonObject data = jsonRequest.getObject("data");

            if(service == null) throw new IllegalArgumentException("The service in the request must be present");
            if(service.trim().isEmpty()) throw new IllegalArgumentException("The service in the request can not be empty");

            if(data == null) throw new IllegalArgumentException("The data in the request must be present");

            final Callable<JsonElement> callable = new Callable<JsonElement>() {
                private JsonElement serviceResponse;
                private Boolean continueProcess = false;

                @Override
                public JsonElement call() throws Exception {

                    SlideshowFXServer.this.platformManager.vertx().eventBus().send(service, data, (Message<JsonElement> message) -> {
                        try { serviceResponse = message.body(); }
                        finally { continueProcess = true; }
                    });

                    while(!continueProcess) {}

                    return serviceResponse;
                }
             };

            final FutureTask<JsonElement> task = new FutureTask<>(callable);
            task.run();
            response = task.get();
        } catch(DecodeException e) {
            throw new IllegalArgumentException("The request is invalid", e);
        } catch(ClassCastException e) {
            LOGGER.log(Level.SEVERE, "An error occurred", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return response;
    }

    /**
     * Return the latest created server.
     *
     * @return The latest created server or null if no server was created.
     */
    public static SlideshowFXServer getSingleton() { return singleton; }
}
