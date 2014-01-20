package com.twasyl.slideshowfx.chat;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.json.impl.Json;
import org.vertx.java.core.streams.Pump;

import java.io.*;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by 100043901 on 19/12/13.
 */
public class Chat {

    private static final Logger LOGGER = Logger.getLogger(Chat.class.getName());

    private static class Twitter {
        private final StringProperty hashtag = new SimpleStringProperty();
        private final BooleanProperty authenticated = new SimpleBooleanProperty(false);

        private Thread streamTwitterThread;
        private OAuthService service;
        private Token accessToken;
        private Twitter(String hashtag) {
            this.hashtag.set(hashtag);
        }

        public StringProperty hashtagProperty() { return this.hashtag; }
        public String getHashtag() { return this.hashtagProperty().get(); }
        public void setHashtag(String hashtag) { this.hashtagProperty().set(hashtag); }

        public BooleanProperty authenticatedProperty() { return this.authenticated; }
        public boolean isAuthenticated() { return this.authenticatedProperty().get(); }
        public void setAuthenticated(boolean authenticated) { this.authenticatedProperty().set(authenticated); }

        /**
         * Connect a user to twitter
         */
        private void connect() {
            if(accessToken == null) {
                this.service = new ServiceBuilder()
                        .provider(TwitterApi.SSL.class)
                        .apiKey("5luxVGxswd42RgTfbF02g")
                        .apiSecret("winWDhMbeJZ4m66gABqpohkclLDixnyeOINuVtPWs")
                        .callback("oob")
                        .build();

                final Token requestToken = this.service.getRequestToken();

                final String authUrl = this.service.getAuthorizationUrl(requestToken);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        final WebView twitterBrowser = new WebView();
                        final Scene scene = new Scene(twitterBrowser);
                        final Stage stage = new Stage();

                        twitterBrowser.getEngine().load(authUrl);

                        twitterBrowser.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
                            @Override
                            public void changed(ObservableValue<? extends Worker.State> observableValue, Worker.State state, Worker.State state2) {
                                if(state2 == Worker.State.SUCCEEDED) {
                                    if (twitterBrowser.getEngine().getDocument().getDocumentURI().equals("https://api.twitter.com/oauth/authorize")) {
                                        String pinCode = twitterBrowser.getEngine().getDocument().getElementsByTagName("kbd").item(0).getTextContent();

                                        Verifier verifier = new Verifier(pinCode);
                                        Twitter.this.accessToken = Twitter.this.service.getAccessToken(requestToken, verifier);

                                        Twitter.this.setAuthenticated(true);

                                        stage.close();
                                    }
                                }
                            }
                        });

                        stage.setScene(scene);
                        stage.show();
                    }
                });
            }
        }

        public void disconnect() {
            if(this.accessToken != null) {
                this.service = null;
            }
        }

        public void streamTweets() {
            if(isAuthenticated()) {
                Runnable streamTwitterRunnable = new Runnable() {
                    @Override
                    public void run() {
                        LOGGER.fine("Start looking for tweets");

                        OAuthRequest request = new OAuthRequest(Verb.POST, "https://stream.twitter.com/1/statuses/filter.json");
                        request.addQuerystringParameter("track", Twitter.this.hashtag.get());
                        request.setConnectionKeepAlive(true);

                        Twitter.this.service.signRequest(Twitter.this.accessToken, request);

                        Response response = request.send();

                        String resultLine = null;
                        BufferedReader reader = null;

                        try {
                            reader = new BufferedReader(new InputStreamReader(response.getStream()));
                            JsonObject jsonResponse;
                            JsonObject jsonResponseMessage;

                            while ((resultLine = reader.readLine()) != null && !resultLine.isEmpty()) {
                                System.out.println(resultLine);
                                JsonObject tweetJson = new JsonObject(resultLine);

                                resultLine = null;

                                jsonResponseMessage = new JsonObject();
                                jsonResponseMessage.putString(JSON_MESSAGE_ID_ATTR, "msg-" + System.currentTimeMillis());
                                jsonResponseMessage.putString(JSON_MESSAGE_AUTHOR_ATTR, "@" + tweetJson.getObject("user").getString("screen_name"));
                                jsonResponseMessage.putString(JSON_MESSAGE_CONTENT_ATTR, Chat.formatChatMessage(tweetJson.getString("text")));

                                jsonResponse = new JsonObject();
                                jsonResponse.putObject(JSON_MESSAGE_OBJECT, jsonResponseMessage);

                                chatHistory.add(jsonResponse.toString());

                                for(ServerWebSocket client : Chat.clients) {
                                    client.writeTextFrame(jsonResponse.toString());
                                }
                            }
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Warning: error while reading the stream from twitter", ex);
                        }
                    }
                };

                this.streamTwitterThread = new Thread(streamTwitterRunnable);
                this.streamTwitterThread.start();
            }
        }

        public void stopStreeamTweets() {
            if(this.streamTwitterThread != null && !this.streamTwitterThread.isAlive()) {
                this.streamTwitterThread.interrupt();
            }
        }
    }

    private static final String VELOCITY_SERVER_IP = "slideshowfx_server_ip";
    private static final String VELOCITY_SERVER_PORT = "slideshowfx_server_port";
    private static final String HTTP_CLIENT_CHAT_PATH = "/";
    private static final String WS_CLIENT_CHAT = "/slideshowfx/chat";
    private static final String HTTP_PRESENTER_CHAT = "/slideshowfx/chat/presenter";
    private static final String WS_PRESENTER_CHAT = HTTP_PRESENTER_CHAT;

    private static final String JSON_MESSAGE_OBJECT = "message";
    private static final String JSON_MESSAGE_ACTION_OBJECT = "messageAction";
    private static final String JSON_MESSAGE_ID_ATTR = "id";
    private static final String JSON_MESSAGE_AUTHOR_ATTR = "author";
    private static final String JSON_MESSAGE_CONTENT_ATTR = "content";
    private static final String JSON_MESSAGE_ACTION_ID_ATTR = "id";
    private static final String JSON_MESSAGE_ACTION_ACTION_ATTR = "action";
    private static final String JSON_MESSAGE_ACTION_ANSWERED_VALUE = "answered";
    private static final String JSON_MESSAGE_ACTION_MARK_READ_VALUE = "mark-read";

    private static final List<ServerWebSocket> clients = new ArrayList<>();
    private static ServerWebSocket presenter;
    private static final List<String> chatHistory = new ArrayList<>();

    private static Vertx vertx;
    private static HttpServer server;
    private static String ip;
    private static int port;
    private static Twitter twitter;

    public Chat(String ipAddress, int port) {

        init();
    }

    public static void create(String ip, int port, String twitterHashtag) {
        Chat.ip = ip;
        Chat.port = port;
        Chat.twitter = new Twitter(twitterHashtag);

        init();
    }

    public static void close() {
        for(ServerWebSocket client : clients) {
            client.close();
        }
        clients.clear();

        if(presenter != null) {
            presenter.close();
            presenter = null;
        }

        if(server != null) {
            server.close();
            server = null;
        }

        if(vertx != null) {
            vertx.stop();
            vertx = null;
        }

        if(Chat.twitter != null) {
            Chat.twitter.stopStreeamTweets();

            Chat.twitter.disconnect();
        }
    }

    public static boolean isOpened() {
        return server != null;
    }
    /**
     * Initialize the embedded web server
     */
    private static void init() {

        vertx = VertxFactory.newVertx();
        server = vertx.createHttpServer();

        server.requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest httpServerRequest) {
                if(HTTP_CLIENT_CHAT_PATH.equals(httpServerRequest.path())) {
                    // Read the chat HTML page, parse it and send it to the client
                    try {
                        final File extractedChatFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "sfx-chatFile.html");
                        LOGGER.fine("Chat HTML file generated: " + extractedChatFile.getAbsolutePath());
                        final FileReader chatPageReader = new FileReader( new File(getClass().getResource("/com/twasyl/slideshowfx/html/chat.html").toURI()));
                        final FileWriter writer = new FileWriter(extractedChatFile);

                        extractedChatFile.deleteOnExit();

                        Velocity.init();
                        final VelocityContext context = new VelocityContext();
                        context.put(VELOCITY_SERVER_IP, ip);
                        context.put(VELOCITY_SERVER_PORT, port);

                        Velocity.evaluate(context, writer, "", chatPageReader);

                        writer.flush();
                        writer.close();

                        chatPageReader.close();

                        httpServerRequest.response().sendFile(extractedChatFile.getAbsolutePath());
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);
                    }
                } else if(HTTP_PRESENTER_CHAT.equals(httpServerRequest.path())) {
                    // Read the chat presenter HTML page, parse it and send it to the client
                    try {
                        final File extractedChatFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "sfx-chatPresenterFile.html");
                        LOGGER.fine("Chat HTML file generated: " + extractedChatFile.getAbsolutePath());
                        final FileReader chatPageReader = new FileReader( new File(getClass().getResource("/com/twasyl/slideshowfx/html/presenter.html").toURI()));
                        final FileWriter writer = new FileWriter(extractedChatFile);

                        extractedChatFile.deleteOnExit();

                        Velocity.init();
                        final VelocityContext context = new VelocityContext();
                        context.put(VELOCITY_SERVER_IP, ip);
                        context.put(VELOCITY_SERVER_PORT, port);

                        Velocity.evaluate(context, writer, "", chatPageReader);

                        writer.flush();
                        writer.close();

                        chatPageReader.close();

                        httpServerRequest.response().sendFile(extractedChatFile.getAbsolutePath());
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error when a client tried to access the chat", e);
                    }
                } else if("/images/check.png".equals(httpServerRequest.path())) {
                    try {
                        File checkImageFile = new File(Chat.class.getResource("/com/twasyl/slideshowfx/html/images/check.png").toURI());
                        httpServerRequest.response().sendFile(checkImageFile.getAbsolutePath());
                    } catch (URISyntaxException e) {
                        LOGGER.log(Level.WARNING, "Can not send check images", e);
                    }
                }
            }
        }).websocketHandler(new Handler<ServerWebSocket>() {
            private final List<ServerWebSocket> clients = new ArrayList<>();

            @Override
            public void handle(final ServerWebSocket serverWebSocket) {
                if (WS_CLIENT_CHAT.equals(serverWebSocket.path())) {
                    Pump.createPump(serverWebSocket, serverWebSocket).start();

                    clients.add(serverWebSocket);

                    // Send chat history
                    for (String historyMessage : chatHistory) {
                        serverWebSocket.writeTextFrame(historyMessage);
                    }
                    serverWebSocket.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void aVoid) {
                            clients.remove(serverWebSocket);
                        }
                    });

                    serverWebSocket.dataHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {
                            long timeStamp = System.currentTimeMillis();

                            JsonObject jsonRequest = new JsonObject(new String(buffer.getBytes()));

                            // Handle a message
                            if (jsonRequest.getObject(JSON_MESSAGE_OBJECT) != null) {
                                JsonObject jsonRequestMessage = jsonRequest.getObject(JSON_MESSAGE_OBJECT);
                                JsonObject jsonResponse;
                                JsonObject jsonResponseMessage;

                                for (ServerWebSocket socket : clients) {
                                    jsonResponseMessage = new JsonObject();

                                    jsonResponseMessage.putString(JSON_MESSAGE_ID_ATTR, "msg-" + timeStamp);

                                    if (socket.remoteAddress().equals(serverWebSocket.remoteAddress())) {
                                        jsonResponseMessage.putString(JSON_MESSAGE_AUTHOR_ATTR, "I");
                                    } else {
                                        jsonResponseMessage.putString(JSON_MESSAGE_AUTHOR_ATTR, jsonRequestMessage.getString(JSON_MESSAGE_AUTHOR_ATTR));
                                    }

                                    jsonResponseMessage.putString(JSON_MESSAGE_CONTENT_ATTR, Chat.formatChatMessage(jsonRequestMessage.getString(JSON_MESSAGE_CONTENT_ATTR)));

                                    jsonResponse = new org.vertx.java.core.json.JsonObject();
                                    jsonResponse.putObject(JSON_MESSAGE_OBJECT, jsonResponseMessage);
                                    socket.writeTextFrame(jsonResponse.toString());
                                }

                                jsonResponseMessage = new JsonObject();
                                jsonResponseMessage.putString(JSON_MESSAGE_ID_ATTR, "msg-" + timeStamp);
                                jsonResponseMessage.putString(JSON_MESSAGE_AUTHOR_ATTR, jsonRequestMessage.getString(JSON_MESSAGE_AUTHOR_ATTR));
                                jsonResponseMessage.putString(JSON_MESSAGE_CONTENT_ATTR, Chat.formatChatMessage(jsonRequestMessage.getString(JSON_MESSAGE_CONTENT_ATTR)));

                                jsonResponse = new JsonObject();
                                jsonResponse.putObject(JSON_MESSAGE_OBJECT, jsonResponseMessage);

                                chatHistory.add(jsonResponse.toString());

                                if (presenter != null) {
                                    presenter.writeTextFrame(jsonResponse.toString());
                                }
                            }
                        }
                    });
                } else if (WS_PRESENTER_CHAT.equals(serverWebSocket.path())) {
                    presenter = serverWebSocket;

                    presenter.dataHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {
                            JsonObject jsonRequest = new JsonObject(new String(buffer.getBytes()));

                            if (jsonRequest.getObject(JSON_MESSAGE_ACTION_OBJECT) != null) {
                                JsonObject jsonMessageAction = jsonRequest.getObject(JSON_MESSAGE_ACTION_OBJECT);

                                JsonObject jsonResponse = new JsonObject();
                                jsonResponse.putString(JSON_MESSAGE_ACTION_ID_ATTR, jsonMessageAction.getString(JSON_MESSAGE_ACTION_ID_ATTR));
                                jsonResponse.putString(JSON_MESSAGE_ACTION_ACTION_ATTR, JSON_MESSAGE_ACTION_ANSWERED_VALUE);

                                JsonObject jsonMessageActionResponse = new JsonObject();
                                jsonMessageActionResponse.putObject(JSON_MESSAGE_ACTION_OBJECT, jsonResponse);

                                for (ServerWebSocket client : clients) {
                                    client.writeTextFrame(jsonMessageActionResponse.toString());
                                }
                            }
                        }
                    });

                    for (String historyMessage : chatHistory) {
                        presenter.writeTextFrame(historyMessage);
                    }
                } else {
                    serverWebSocket.reject();
                }
            }


        }).listen(port, ip);

        // Twitter initialization
        if(Chat.twitter.getHashtag() != null && !Chat.twitter.getHashtag().isEmpty()) {
            twitter.connect();
            twitter.authenticatedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean aBoolean2) {
                    if(aBoolean2) {
                        twitter.streamTweets();
                    }
                }
            });
        }
    }

    public static String getIp() { return ip; }

    public static int getPort() { return port; }

    private static String formatChatMessage(final String message) {
        return message.replaceAll("\\n", "&#10;")
                .replaceAll("\\\\", "&#92;")
                .replace("\'", "&#39;");
    }
}
