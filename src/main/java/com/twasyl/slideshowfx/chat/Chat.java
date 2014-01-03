package com.twasyl.slideshowfx.chat;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.streams.Pump;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by 100043901 on 19/12/13.
 */
public class Chat {

    private static final Logger LOGGER = Logger.getLogger(Chat.class.getName());

    private static final String VELOCITY_SERVER_IP = "slideshowfx_server_ip";
    private static final String VELOCITY_SERVER_PORT = "slideshowfx_server_port";
    private static final String HTTP_CLIENT_CHAT_PATH = "/";
    private static final String WS_CLIENT_CHAT = "/slideshowfx/chat";
    private static final String HTTP_PRESENTER_CHAT = "/slideshowfx/chat/presenter";
    private static final String WS_PRESENTER_CHAT = HTTP_PRESENTER_CHAT;

    private static final List<ServerWebSocket> clients = new ArrayList<>();
    private static ServerWebSocket presenter;

    private static HttpServer server;
    private static String ip;
    private static int port;

    public Chat(String ipAddress, int port) {


        init();
    }

    public static void create(String ip, int port) {
        Chat.ip = ip;
        Chat.port = port;

        init();
    }

    public static void close() {
        clients.clear();
        server.close();
    }

    /**
     * Initialize the embedded web server
     */
    private static void init() {

        Vertx vertx = VertxFactory.newVertx();
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
                }
            }
        }).websocketHandler(new Handler<ServerWebSocket>() {
            private final List<ServerWebSocket> clients = new ArrayList<>();

            @Override
            public void handle(final ServerWebSocket serverWebSocket) {
                if (WS_CLIENT_CHAT.equals(serverWebSocket.path())) {
                    Pump.createPump(serverWebSocket, serverWebSocket).start();

                    clients.add(serverWebSocket);

                    serverWebSocket.closeHandler(new Handler<Void>() {
                        @Override
                        public void handle(Void aVoid) {
                            clients.remove(serverWebSocket);
                        }
                    });

                    serverWebSocket.dataHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer buffer) {
                            JsonReader reader = Json.createReader(new ByteArrayInputStream(buffer.getBytes()));
                            JsonObject jsonMessage = reader.readObject().getJsonObject("message");

                            for (ServerWebSocket socket : clients) {

                                StringBuffer response = new StringBuffer("<div class=\"chat-message\"><span class=\"author\">");

                                if (socket.remoteAddress().equals(serverWebSocket.remoteAddress())) {
                                    response.append("I ");
                                } else {
                                    response.append(jsonMessage.getString("name")).append(" ");
                                }

                                response.append("said</span> :<br /><span class=\"message-content\">");
                                response.append(jsonMessage.getString("message"));
                                response.append("</span></div>");

                                socket.writeTextFrame(response.toString());
                            }

                            if (presenter != null) {
                                StringBuffer response = new StringBuffer("<div class=\"chat-message\">");
                                response.append("<span class=\"author\">").append(jsonMessage.getString("name")).append(" said :</span><br />");
                                response.append("<span class=\"message-content\">").append(jsonMessage.getString("message")).append("</span></div>");

                                presenter.writeTextFrame(response.toString());
                            }
                        }
                    });
                } else if (WS_PRESENTER_CHAT.equals(serverWebSocket.path())) {
                    presenter = serverWebSocket;
                } else {
                    serverWebSocket.reject();
                }
            }


        }).listen(port, ip);
    }

    public static String getIp() { return ip; }

    public static int getPort() { return port; }
}
