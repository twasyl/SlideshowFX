package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService;
import com.twasyl.slideshowfx.server.service.ISlideshowFXServices;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import com.twasyl.slideshowfx.utils.beans.Wrapper;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService.JSON_KEY_DATA;
import static com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService.JSON_KEY_SERVICE;

/**
 * This class represents an embedded server which is provided in SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class SlideshowFXServer {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFXServer.class.getName());

    private static SlideshowFXServer singleton;

    private Vertx vertx;
    private HttpServer httpServer;
    private Router router;

    /**
     * The context path of the server for accessing the server's application and resources.
     */
    public static final String CONTEXT_PATH = "/slideshowfx";

    /**
     * The shared data that contains all tokens that can be found in templates.
     */
    public static final String SHARED_DATA_TEMPLATE_TOKENS = "template.tokens";

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
    private String twitterHashtag;
    private final Set<ServerWebSocket> websockets = new HashSet<>();

    private SlideshowFXServer(String host, int port, String twitterHashtag) {
        this.host = host;
        this.port = port;
        this.twitterHashtag = twitterHashtag;
    }

    /**
     * Deploy a {@link io.vertx.core.Verticle} identified by the given class.
     *
     * @param clazz The class of the Verticle to deploy.
     */
    public void deploy(Class<? extends AbstractVerticle> clazz) {
        URL[] classpath = new URL[0];

        try {
            this.vertx.deployVerticle(clazz.newInstance(), result -> {
                if (result.succeeded()) {
                    LOGGER.log(Level.FINE, "Verticle has been deployed successfully. Result: " + result.result());
                } else if (result.failed()) {
                    LOGGER.log(Level.WARNING, "Verticle hasn't been deployed properly. Result: " + result.result(), result.cause());
                }
            });
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Verticle hasn't been deployed properly.", e);
        }
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
     * Get the Twitter hashtag to look on Twitter.
     * @return The Twitter hashtag to look on Twitter.
     */
    public String getTwitterHashtag() { return this.twitterHashtag; }

    /**
     * Get the HTTP server that has been created with this server.
     * @return The HTTP server created by this server.
     */
    public HttpServer getHttpServer() { return httpServer; }

    /**
     * Get the {@link Router} that has been created with this server.
     * @return The Router created by this server.
     */
    public Router getRouter() { return this.router; }

    public Set<ServerWebSocket> getWebSockets() {
        return this.websockets;
    }

    /**
     * Starts the embedded server.
     * @param services List of classes extending {@link ISlideshowFXServices} that have to be started with the
     *                 server
     */
    public void start(Class<? extends ISlideshowFXServices> ... services) {

        if (this.vertx == null) {
            this.vertx = Vertx.vertx();

            /*
             * Add some useful resources that can be shared by multiple Verticles.
             * One of them is a map named "template.tokens" which contains tokens that can be found in a template.
             */
            LocalMap<String, String> templateTokens = this.vertx.sharedData().getLocalMap(SHARED_DATA_TEMPLATE_TOKENS);
            templateTokens.put(SHARED_DATA_SERVER_HOST_TOKEN, "slideshowfx_server_ip");
            templateTokens.put(SHARED_DATA_SERVER_PORT_TOKEN, "slideshowfx_server_port");

            this.httpServer = this.vertx.createHttpServer();
            this.httpServer.websocketHandler(this.buildWebSocketHandler());

            this.router = Router.router(this.vertx);
            this.buildRouter();
            this.httpServer.requestHandler(this.router::accept);

            if(services != null && services.length > 0) {
                Arrays.stream(services).forEach(service -> this.vertx.deployVerticle(service.getName()));
            }

            this.httpServer.listen(SlideshowFXServer.getSingleton().getPort(), SlideshowFXServer.getSingleton().getHost());
        } else {
            LOGGER.log(Level.INFO, "Server already started");
        }
    }

    /**
     * Stops the embedded server.
     */
    public void stop() {
        this.vertx.close();
        this.vertx = null;
        this.httpServer = null;
        this.router = null;
        this.websockets.clear();
        singleton = null;
    }

    /**
     * <p>Call a service using the EventBus of Vert.x. The request must be a JSON object with a field named {@code service}
     * representing the service to call, and a JSON object named {@code data} containing the data the service will
     * consume.</p>
     * <p>The response's format will be a JSON object with:</p>
     * <ul>
     *     <li>the <b>service</b> key which indicates the name of the service which is called, as a string ;</li>
     *     <li>the <b>status</b> key which represents the HTML return code of the service. 200 if everything went well,
     *     ..., as an integer ;</li>
     *     <li>the <b>content</b> key which is the response's content returned by the service, as a JSON structured
     *     (an object, an array, etc) depending on the service</li>
     * </ul>
     * @param request The JSON object corresponding to the service to call.
     * @return The response corresponding to the request.
     * @throws java.lang.IllegalArgumentException If the request is invalid.
     */
    public JsonObject callService(String request) throws IllegalArgumentException {
        final Wrapper<JsonObject> response = new Wrapper<>();

        try {
            final JsonObject jsonRequest = new JsonObject(request);

            final String service = jsonRequest.getString(JSON_KEY_SERVICE);
            final JsonObject data = jsonRequest.getJsonObject(JSON_KEY_DATA);

            if(service == null) throw new IllegalArgumentException("The service in the request must be present");
            if(service.trim().isEmpty()) throw new IllegalArgumentException("The service in the request can not be empty");

            if(data == null) throw new IllegalArgumentException("The data in the request must be present");

            final Thread thread = new Thread(new Runnable() {
                private Boolean continueProcess = false;

                @Override
                public void run() {
                    SlideshowFXServer.this.vertx.eventBus().send(service, data, (AsyncResult<Message<JsonObject>> ar) -> {
                        response.setValue(ar.result().body());
                        continueProcess = true;
                    });

                    while(!continueProcess) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            LOGGER.log(Level.SEVERE, "Can not wait result when calling a service", e);
                        }
                    }
                }
            });

            thread.start();
            thread.join();
        } catch(DecodeException e) {
            throw new IllegalArgumentException("The request is invalid", e);
        } catch(ClassCastException e) {
            LOGGER.log(Level.SEVERE, "An error occurred", e);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "An error occurred", e);
        }

        return response.getValue();
    }

    /**
     * Build the {@link Router} that will handle shared routes.
     */
    private void buildRouter() {
        final BodyHandler handler = BodyHandler.create();
        handler.setMergeFormAttributes(false);
        router.route().handler(handler);

        // Get the main page
        router.get(CONTEXT_PATH).handler(routingContext -> {
            final LocalMap<String, String> templateTokens = this.vertx.sharedData().getLocalMap(SlideshowFXServer.SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getHtmlConfiguration();

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_HOST_TOKEN).toString(), this.getHost());
            tokenValues.put(templateTokens.get(SlideshowFXServer.SHARED_DATA_SERVER_PORT_TOKEN).toString(), this.getPort() + "");

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("slideshowfx.html");
                template.process(tokenValues, writer);

                writer.flush();

                routingContext.response().setStatusCode(200).setChunked(true).write(writer.toString()).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);

                routingContext.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(Level.WARNING, "Error when processing the chat template", e);
                routingContext.response().setStatusCode(500).end();
            }
        });
        router.get(CONTEXT_PATH.concat("/images/logo.svg")).handler(routingContext -> {
            try (final InputStream in = ResourceHelper.getInputStream("/com/twasyl/slideshowfx/images/logo.svg")) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = Buffer.buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                routingContext.response().headers().set("Content-Type", "image/svg+xml");
                routingContext.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Can not send check images", e);
            }
        });
    }

    /**
     * Build the WebSocket handler for handling shared WebSocket calls.
     * @return
     */
    private Handler<ServerWebSocket> buildWebSocketHandler() {
        final Handler<ServerWebSocket> handler = serverWebSocket -> {
            if (CONTEXT_PATH.equals(serverWebSocket.path())) {
                // Add the textHandlerID to the list of WebSocket clients
                this.websockets.add(serverWebSocket);

                // When the socket is closed, remove it from the list of clients
                serverWebSocket.endHandler(event -> {
                    this.websockets.remove(serverWebSocket);
                });

                /*
                 * When data are received, get the content which is expected to be a JSON object with two fields:
                 * <ul>
                 *     <li>service: representing the service to call using the EventBus</li>
                 *     <li>data: representing a JSON object containing the data that is expected by the service.</li>
                 * </ul>
                 */
                serverWebSocket.handler(buffer -> {
                    final String bufferString = new String(buffer.getBytes());
                    final JsonObject request = new JsonObject(bufferString);

                    // Inject source caller so the service, if it needs to, can send something to all WebSocket clients
                    // but exclude the "sender"
                    final JsonObject data = request.getJsonObject(AbstractSlideshowFXService.JSON_KEY_DATA);
                    data.put(AbstractSlideshowFXService.JSON_KEY_ORIGIN, serverWebSocket.textHandlerID());

                    this.vertx.eventBus().send(request.getString(JSON_KEY_SERVICE), data, asyncResult -> {
                        final JsonObject json = (JsonObject) asyncResult.result().body();

                        serverWebSocket.write(Buffer.buffer(json.encode()));
                    });
                });
            } else {
                serverWebSocket.reject();
            }
        };

        return handler;
    }

    /**
     * Return the latest created server.
     *
     * @return The latest created server or null if no server was created.
     */
    public static SlideshowFXServer getSingleton() { return singleton; }

    /**
     * <p>Creates an instance of the {@link SlideshowFXServer}. The created instance will act as singleton and if a previous
     * instance exists, it is stopped by the method.</p>
     * <p>This method doesn't start the server. If you want to start it, please use {@link #start(Class[])})}.</p>
     *
     * @param host The hostname the server will listen on.
     * @param port The port the server will listen on.
     * @param twitterHashtag A Twitter hashtag if needed.
     * @return The instance of the {@link SlideshowFXServer} that has been created.
     */
    public static SlideshowFXServer create(final String host, final int port, final String twitterHashtag) {
        if(singleton != null) {
            singleton.stop();
        }

        singleton = new SlideshowFXServer(host, port, twitterHashtag);
        return singleton;
    }
}
