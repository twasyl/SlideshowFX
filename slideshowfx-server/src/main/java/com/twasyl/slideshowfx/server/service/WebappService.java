package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.icons.FontAwesome;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.utils.TemplateProcessor;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static com.twasyl.slideshowfx.server.SlideshowFXServer.*;
import static java.util.logging.Level.WARNING;

public class WebappService extends AbstractSlideshowFXService {
    private static final Logger LOGGER = Logger.getLogger(WebappService.class.getName());

    @Override
    public void start() {
        SlideshowFXServer.getSingleton().getHttpServer().websocketHandler(this.buildWebSocketHandler());
        this.updateRouteMatcher();
    }

    private void updateRouteMatcher() {
        final SlideshowFXServer singleton = SlideshowFXServer.getSingleton();
        final Router router = singleton.getRouter();

        manageMainPage(router);
        manageLogo(router);
        manageFontAwesome(router);
        manageCSS(router);
    }

    /**
     * Manages access to the main page of the web app.
     *
     * @param router The router to update.
     */
    private void manageMainPage(Router router) {
        final SlideshowFXServer singleton = SlideshowFXServer.getSingleton();

        router.get(CONTEXT_PATH).handler(routingContext -> {
            final LocalMap<String, String> templateTokens = this.vertx.sharedData().getLocalMap(SHARED_DATA_TEMPLATE_TOKENS);

            final Configuration configuration = TemplateProcessor.getHtmlConfiguration(SlideshowFXServer.class);

            final Map tokenValues = new HashMap();
            tokenValues.put(templateTokens.get(SHARED_DATA_SERVER_HOST_TOKEN), singleton.getHost());
            tokenValues.put(templateTokens.get(SHARED_DATA_SERVER_PORT_TOKEN), singleton.getPort() + "");

            try (final StringWriter writer = new StringWriter()) {
                final Template template = configuration.getTemplate("slideshowfx.html");
                template.process(tokenValues, writer);

                writer.flush();

                routingContext.response().setStatusCode(200).setChunked(true).write(writer.toString()).end();
            } catch (IOException e) {
                LOGGER.log(WARNING, "Error when a client tried to access the chat", e);

                routingContext.response().setStatusCode(500).end();
            } catch (TemplateException e) {
                LOGGER.log(WARNING, "Error when processing the chat template", e);
                routingContext.response().setStatusCode(500).end();
            }
        });
    }

    /**
     * Manages access to the logo of the web app.
     *
     * @param router The router to update.
     */
    private void manageLogo(Router router) {
        router.get(CONTEXT_PATH.concat("/images/logo.svg")).handler(routingContext -> {
            try (final InputStream in = SlideshowFXServer.class.getResourceAsStream("/com/twasyl/slideshowfx/server/webapp/images/logo.svg")) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = Buffer.buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                routingContext.response().headers().set("Content-Type", "image/svg+xml");
                routingContext.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not send check images", e);
            }
        });
    }

    /**
     * Manages access to the FontAwesome resources of the web app.
     *
     * @param router The router to update.
     */
    private void manageFontAwesome(Router router) {
        final String FONT_AWESOME_PREFIX = "/slideshowfx/font-awesome/";
        router.get(FONT_AWESOME_PREFIX.concat("*")).handler(routingContext -> {
            final String file = routingContext.request().path().substring(FONT_AWESOME_PREFIX.length());

            try (final InputStream in = FontAwesome.getFontAwesomeFile(file).openStream()) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = Buffer.buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                routingContext.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not send the font awesome file", e);
            }
        });
    }

    /**
     * Manages access to the CSS of the web app.
     *
     * @param router The router to update.
     */
    private void manageCSS(Router router) {
        router.get("/slideshowfx/css/slideshowfx.css").handler(request -> {
            try (final InputStream in = WebappService.class.getResourceAsStream("/com/twasyl/slideshowfx/server/webapp/css/slideshowfx.css")) {

                byte[] imageBuffer = new byte[1028];
                int numberOfBytesRead;
                Buffer buffer = Buffer.buffer();

                while ((numberOfBytesRead = in.read(imageBuffer)) != -1) {
                    buffer.appendBytes(imageBuffer, 0, numberOfBytesRead);
                }

                request.response().setChunked(true).write(buffer).end();
            } catch (IOException e) {
                LOGGER.log(WARNING, "Can not send the font awesome file", e);
            }
        });
    }


    /**
     * Build the WebSocket handler for handling shared WebSocket calls.
     *
     * @return
     */
    private Handler<ServerWebSocket> buildWebSocketHandler() {
        return serverWebSocket -> {
            if (CONTEXT_PATH.equals(serverWebSocket.path())) {
                // Add the textHandlerID to the list of WebSocket clients
                final Set<ServerWebSocket> websockets = SlideshowFXServer.getSingleton().getWebSockets();
                websockets.add(serverWebSocket);

                // When the socket is closed, remove it from the list of clients
                serverWebSocket.endHandler(event -> websockets.remove(serverWebSocket));

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
                    final JsonObject data = request.getJsonObject(JSON_KEY_DATA);
                    data.put(JSON_KEY_ORIGIN, serverWebSocket.textHandlerID());

                    this.vertx.eventBus().request(request.getString(JSON_KEY_SERVICE), data, asyncResult -> {
                        final JsonObject json = (JsonObject) asyncResult.result().body();

                        serverWebSocket.write(Buffer.buffer(json.encode()));
                    });
                });
            } else {
                serverWebSocket.reject();
            }
        };
    }
}
