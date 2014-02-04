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

package com.twasyl.slideshowfx.chat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
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
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.streams.Pump;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
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
                            ChatMessage chatMessage = null;

                            while ((resultLine = reader.readLine()) != null && !resultLine.isEmpty()) {
                                JsonObject tweetJson = new JsonObject(resultLine);

                                resultLine = null;

                                chatMessage = new ChatMessage();
                                chatMessage.setId(System.currentTimeMillis() + "");
                                chatMessage.setSource(ChatMessageSource.TWITTER);
                                chatMessage.setStatus(ChatMessageStatus.NEW);
                                chatMessage.setAuthor("@" + tweetJson.getObject("user").getString("screen_name"));
                                chatMessage.setContent(tweetJson.getString("text"));

                                chatHistory.put(chatMessage.getId(), chatMessage);

                                for(ServerWebSocket client : Chat.clients) {
                                    client.writeTextFrame(chatMessage.toJSON());
                                }

                                if(Chat.presenter != null) { Chat.presenter.writeTextFrame(chatMessage.toJSON()); }
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

    private static final List<ServerWebSocket> clients = new ArrayList<>();
    private static ServerWebSocket presenter;
    private static final Map<String, ChatMessage> chatHistory = new HashMap<>();

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
                } else if("/images/chatQRCode.png".equals(httpServerRequest.path())) {
                    httpServerRequest.response().setChunked(true);
                    httpServerRequest.response().headers().set("Content-Type", "image/png");

                    Buffer buffer = new Buffer(Chat.generateQRCode(350))    ;
                    httpServerRequest.response().write(buffer);
                    httpServerRequest.response().end();
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
                    for (ChatMessage historyMessage : chatHistory.values()) {
                        serverWebSocket.writeTextFrame(historyMessage.toJSON(serverWebSocket.remoteAddress()));
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

                            if (presenter != null) presenter.writeTextFrame(chatMessage.toJSON());

                            for (ServerWebSocket socket : clients) {
                                socket.writeTextFrame(chatMessage.toJSON(socket.remoteAddress()));
                            }
                        }
                    });
                } else if (WS_PRESENTER_CHAT.equals(serverWebSocket.path())) {
                    presenter = serverWebSocket;

                    presenter.dataHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {
                            ChatMessage chatMessage = null;
                            try {
                                chatMessage = ChatMessage.build(new String(buffer.getBytes(), "UTF-8"), serverWebSocket.remoteAddress());
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            ChatMessageStatus msgStatus = null;
                            if (chatMessage.getAction() == ChatMessageAction.MARK_READ) {
                                msgStatus = ChatMessageStatus.ANSWERED;
                            }

                            chatMessage = Chat.chatHistory.get(chatMessage.getId());
                            chatMessage.setStatus(msgStatus);
                            chatMessage.setAction(null);

                            for (ServerWebSocket client : clients) {
                                client.writeTextFrame(chatMessage.toJSON(client.remoteAddress()));
                            }
                        }
                    });

                    for (ChatMessage historyMessage : chatHistory.values()) {
                        presenter.writeTextFrame(historyMessage.toJSON());
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

    public static byte[] generateQRCode(int size) {
        byte[] qrCode = null;

        final String qrCodeData = String.format("http://%1$s:%2$s%3$s",
            getIp(), getPort(), HTTP_CLIENT_CHAT_PATH);

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
