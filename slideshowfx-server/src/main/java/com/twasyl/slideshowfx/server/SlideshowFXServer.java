package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.ISlideshowFXServices;
import com.twasyl.slideshowfx.utils.beans.Wrapper;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService.JSON_KEY_DATA;
import static com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService.JSON_KEY_SERVICE;
import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.toList;

/**
 * This class represents an embedded server which is provided in SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class SlideshowFXServer {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFXServer.class.getName());

    private static SlideshowFXServer singleton;
    private static final Object singletonLock = new Object();

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
     * Get the host of the server.
     *
     * @return The host of the server.
     */
    public String getHost() {
        return host;
    }

    /**
     * Get the port of the server
     *
     * @return The port of the server.
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the Twitter hashtag to look on Twitter.
     *
     * @return The Twitter hashtag to look on Twitter.
     */
    public String getTwitterHashtag() {
        return this.twitterHashtag;
    }

    /**
     * Get the HTTP server that has been created with this server.
     *
     * @return The HTTP server created by this server.
     */
    public HttpServer getHttpServer() {
        return httpServer;
    }

    /**
     * Get the {@link Router} that has been created with this server.
     *
     * @return The Router created by this server.
     */
    public Router getRouter() {
        return this.router;
    }

    public Set<ServerWebSocket> getWebSockets() {
        return this.websockets;
    }

    /**
     * Starts the embedded server.
     *
     * @param services List of classes extending {@link ISlideshowFXServices} that have to be started with the
     *                 server
     * @return
     */
    public CompletableFuture<Void> start(Class<? extends ISlideshowFXServices>... services) {

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

            this.router = Router.router(this.vertx);
            this.buildRouter();
            this.httpServer.requestHandler(this.router);

            return deployServices(services).thenRunAsync(() -> this.httpServer.listen(SlideshowFXServer.getSingleton().getPort(), SlideshowFXServer.getSingleton().getHost()));
        } else {
            LOGGER.log(Level.INFO, "Server already started");
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> deployServices(Class<? extends ISlideshowFXServices>... services) {
        if (services != null && services.length > 0) {
            final List<CompletableFuture<Void>> allDeployments = Arrays.stream(services).map(this::deployService).collect(toList());

            return CompletableFuture.allOf(allDeployments.toArray(new CompletableFuture[0]));
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private CompletableFuture<Void> deployService(final Class<? extends ISlideshowFXServices> service) {
        final var handler = new ServiceDeploymentHandler(service);
        this.vertx.deployVerticle(service.getName(), handler);
        return handler.future();
    }

    /**
     * Stops the embedded server.
     *
     * @return A {@link CompletableFuture} representing the stopping process.
     */
    public CompletableFuture<Void> stop() {
        final CompletableFuture<Void> stoppingTask = new CompletableFuture<>();

        if (this.vertx != null) {
            this.vertx.close(event -> {
                if (event.succeeded()) {
                    this.vertx = null;
                    this.httpServer = null;
                    this.router = null;
                    this.websockets.clear();
                    resetSingleton();
                    stoppingTask.complete(null);
                } else {
                    stoppingTask.completeExceptionally(event.cause());
                }
            });
        }

        return stoppingTask;
    }

    /**
     * <p>Call a service using the EventBus of Vert.x. The request must be a JSON object with a field named {@code service}
     * representing the service to call, and a JSON object named {@code data} containing the data the service will
     * consume.</p>
     * <p>The response's format will be a JSON object with:</p>
     * <ul>
     * <li>the <b>service</b> key which indicates the name of the service which is called, as a string ;</li>
     * <li>the <b>status</b> key which represents the HTML return code of the service. 200 if everything went well,
     * ..., as an integer ;</li>
     * <li>the <b>content</b> key which is the response's content returned by the service, as a JSON structured
     * (an object, an array, etc) depending on the service</li>
     * </ul>
     *
     * @param request The JSON object corresponding to the service to call.
     * @return The response corresponding to the request.
     * @throws java.lang.IllegalArgumentException If the request is invalid.
     */
    public JsonObject callService(String request) {
        final Wrapper<JsonObject> response = new Wrapper<>();

        try {
            final var jsonRequest = new JsonObject(request);
            final var service = jsonRequest.getString(JSON_KEY_SERVICE);
            final var data = jsonRequest.getJsonObject(JSON_KEY_DATA);

            if (service == null) throw new IllegalArgumentException("The service in the request must be present");
            if (service.trim().isEmpty())
                throw new IllegalArgumentException("The service in the request can not be empty");

            if (data == null) throw new IllegalArgumentException("The data in the request must be present");

            final var thread = new Thread(new Runnable() {
                private boolean continueProcess = false;

                @Override
                public void run() {
                    SlideshowFXServer.this.vertx.eventBus().request(service, data, (AsyncResult<Message<JsonObject>> ar) -> {
                        response.setValue(ar.result().body());
                        continueProcess = true;
                    });

                    while (!continueProcess) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            LOGGER.log(SEVERE, "Can not wait result when calling a service", e);
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            });

            thread.start();
            thread.join();
        } catch (DecodeException e) {
            throw new IllegalArgumentException("The request is invalid", e);
        } catch (ClassCastException e) {
            LOGGER.log(SEVERE, "An error occurred", e);
        } catch (InterruptedException e) {
            LOGGER.log(SEVERE, "An error occurred", e);
            Thread.currentThread().interrupt();
        }

        return response.getValue();
    }

    /**
     * Build the {@link Router} that will handle shared routes.
     */
    private void buildRouter() {
        final var handler = BodyHandler.create();
        handler.setMergeFormAttributes(false);
        router.route().handler(handler);
    }

    /**
     * Return the latest created server.
     *
     * @return The latest created server or null if no server was created.
     */
    public static SlideshowFXServer getSingleton() {
        synchronized (singletonLock) {
            return singleton;
        }
    }

    /**
     * Reset the singleton instance of the server.
     */
    private static void resetSingleton() {
        synchronized (singletonLock) {
            singleton = null;
        }
    }

    /**
     * <p>Creates an instance of the {@link SlideshowFXServer}. The created instance will act as singleton and if a previous
     * instance exists, it is stopped by the method.</p>
     * <p>This method doesn't start the server. If you want to start it, please use {@link #start(Class[])})}.</p>
     *
     * @param host           The hostname the server will listen on.
     * @param port           The port the server will listen on.
     * @param twitterHashtag A Twitter hashtag if needed.
     * @return The instance of the {@link SlideshowFXServer} that has been created.
     */
    public static SlideshowFXServer create(final String host, final int port, final String twitterHashtag) {
        synchronized (singletonLock) {
            if (singleton != null && singleton.vertx != null) {
                singleton.stop();
            }

            singleton = new SlideshowFXServer(host, port, twitterHashtag);

            return singleton;
        }
    }
}
