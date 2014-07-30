package com.twasyl.slideshowfx.server;

import com.twasyl.slideshowfx.server.service.AttendeeChatService;
import com.twasyl.slideshowfx.server.service.PresenterChatService;
import com.twasyl.slideshowfx.server.service.TwitterService;
import org.vertx.java.platform.PlatformLocator;
import org.vertx.java.platform.PlatformManager;

import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents an embedded server which is provided in SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class SlideshowFXServer {

    private static final Logger LOGGER = Logger.getLogger(SlideshowFXServer.class.getName());

    private static SlideshowFXServer singleton;

    private PlatformManager platformManager;

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

        deploy(AttendeeChatService.class);
        deploy(PresenterChatService.class);
        deploy(TwitterService.class);
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

        this.platformManager = null;
        singleton = null;
    }

    /**
     * Return the latest created server.
     *
     * @return The latest created server or null if no server was created.
     */
    public static SlideshowFXServer getSingleton() { return singleton; }
}
