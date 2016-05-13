package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * This class represents the base class for creating a SlideshowFX service.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class AbstractSlideshowFXService extends AbstractVerticle implements ISlideshowFXServices {

    public static final String JSON_KEY_BROADCAST_MESSAGE_TYPE = "type";
    public static final String JSON_KEY_SERVICE = "service";
    public static final String JSON_KEY_DATA = "data";
    public static final String JSON_KEY_CODE = "code";
    public static final String JSON_KEY_CONTENT = "content";
    public static final String JSON_KEY_ORIGIN = "origin";
    public static final String JSON_KEY_MESSAGE = "message";
    public static final String JSON_KEY_MESSAGE_ID = "id";
    public static final String JSON_KEY_MESSAGE_STATUS = "status";
    public static final String JSON_KEY_MESSAGE_ACTION = "action";
    public static final String JSON_KEY_FIELDS = "fields";
    public static final String JSON_KEY_FIELD_STATUS = "status";
    public static final String JSON_KEY_FIELD_ACTION = "action";

    private Map<String, MessageConsumer> messageConsumers = new HashMap<>();

    /**
     * Get a {@link MessageConsumer} identified by its endpoint.
     * @param endpoint The endpoint to access the {@link MessageConsumer}.
     * @return The {@link MessageConsumer} identified by its endpoint or {@code null} if it is not found.
     * @throws NullPointerException if the {@code endpoint} is null.
     * @throws IllegalArgumentException If the {@code endpoint} is empty
     */
    public MessageConsumer getMessageConsumer(final String endpoint) {
        if(endpoint == null) throw new NullPointerException("The endpoint can not be null");
        if(endpoint.trim().isEmpty()) throw new NullPointerException("The endpoint can not be empty");

        return this.messageConsumers.get(endpoint.trim());
    }

    /**
     * Register the given {@code handler} with the given {@code endpoint} to this Vert.x EventBus instance.
     * @param endpoint The endpoint for accessing the consumer.
     * @param handler The handler to register.
     * @return This instance of service.
     * @throws NullPointerException If the endpoint or the handler is {@code null}.
     * @throws IllegalArgumentException If the endpoint is empty
     */
    public AbstractSlideshowFXService register(final String endpoint, final Handler<Message<JsonObject>> handler) {
        if(endpoint == null) throw new NullPointerException("The endpoint can not be null");
        if(endpoint.trim().isEmpty()) throw new NullPointerException("The endpoint can not be empty");
        if(handler == null) throw new NullPointerException("The handler can not be null");

        final MessageConsumer consumer = this.vertx.eventBus().consumer(endpoint, handler);
        this.messageConsumers.put(endpoint, consumer);

        return this;
    }

    /**
     * Unregister all handler of messages in the EventBus.
     */
    public void unregisterAll() {
        this.messageConsumers.forEach((endpoint, consumer) -> consumer.unregister());
    }

    /**
     * Build a response to be send when a service is called. The response is a JSON object with the structure expected
     * by the {@link com.twasyl.slideshowfx.server.SlideshowFXServer#callService(String)} method.
     * @param serviceEndpoint The endpoint of the service producing the response.
     * @param responseCode The response code of the service's call.
     * @param responseContent The content of the response.
     * @return The JSON structure that can be send by a service caller.
     */
    protected JsonObject buildResponse(final String serviceEndpoint, final int responseCode, final Object responseContent) {
        final JsonObject response = new JsonObject()
                .put(JSON_KEY_SERVICE, serviceEndpoint)
                .put(JSON_KEY_CODE, responseCode)
                .put(JSON_KEY_CONTENT, responseContent);

        return response;
    }

    /**
     * <p>Send a given response to all WebSocket clients excluding the origin. If the given origin is {@code null}
     * or empty, no exclusion will be performed.</p>
     *
     * @param response The response to send.
     * @param excludeOrigin The origin to exclude, if needed.
     */
    protected void sendResponseToWebSocketClients(final JsonObject response, final String excludeOrigin) {
        SlideshowFXServer.getSingleton().getWebSockets().forEach(socket -> {
            if(!socket.textHandlerID().equals(excludeOrigin)) {
                socket.write(Buffer.buffer(response.encode()));
            }
        });
    }

    /**
     * <p>Send a given response to all WebSocket clients.</p>
     *
     * @param response The response to send.
     */
    protected void sendResponseToWebSocketClients(final JsonObject response) {
        this.sendResponseToWebSocketClients(response, null);
    }
}
