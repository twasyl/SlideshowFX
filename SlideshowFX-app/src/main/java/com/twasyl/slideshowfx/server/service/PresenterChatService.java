package com.twasyl.slideshowfx.server.service;

import com.twasyl.slideshowfx.controls.slideshow.SlideshowPane;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static com.twasyl.slideshowfx.server.service.IServicesCode.RESPONSE_CODE_MESSAGE_ADDED;

/**
 * This class represents the presenter part of the internal SlideshowFX chat.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class PresenterChatService extends AbstractSlideshowFXService {

    @Override
    public void start() {
        this.register(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, this.buildMessageHandler());
    }

    @Override
    public void stop() { this.unregisterAll(); }

    private Handler<Message<JsonObject>> buildMessageHandler() {
        final Handler<Message<JsonObject>> handler = message -> {
            final JsonObject messageContent = message.body();

            if(SlideshowPane.getSingleton() != null) SlideshowPane.getSingleton().publishMessage(messageContent);

            message.reply(this.buildResponse(SERVICE_CHAT_PRESENTER_MESSAGE_ADD, RESPONSE_CODE_MESSAGE_ADDED, ""));
        };

        return handler;
    }
}
