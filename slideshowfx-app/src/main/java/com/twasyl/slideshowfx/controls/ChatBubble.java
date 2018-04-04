package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.server.SlideshowFXServer;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.service.ISlideshowFXServices;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static com.twasyl.slideshowfx.server.service.AbstractSlideshowFXService.*;
/**
 * <p>This class is used to display a {@link com.twasyl.slideshowfx.server.beans.chat.ChatMessage} in the
 * {@link com.twasyl.slideshowfx.controls.slideshow.SlideshowPane}. A {@link ChatBubble} is inserted inside a
 * {@link ChatPanel}.</p>
 * <p>When creating a bubble, the {@link ChatBubble} will respond to a double click in order to update the message
 * it contains.</p>
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public class ChatBubble extends VBox {

    private final ObjectProperty<ChatMessage> chatMessage = new SimpleObjectProperty<>();
    private final Text authorLabel = new Text();
    private final Text messageContent = new Text();
    private final PseudoClass answeredPseudoClass = PseudoClass.getPseudoClass("answered");

    public ChatBubble() {
        this.getStyleClass().add("chat-bubble");

        this.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                this.pseudoClassStateChanged(this.answeredPseudoClass, true);
                this.authorLabel.pseudoClassStateChanged(this.answeredPseudoClass, true);
                this.messageContent.pseudoClassStateChanged(this.answeredPseudoClass, true);

                final JsonObject request = new JsonObject();
                request.put(JSON_KEY_SERVICE, ISlideshowFXServices.SERVICE_CHAT_ATTENDEE_MESSAGE_UPDATE)
                        .put(JSON_KEY_DATA, new JsonObject()
                                                .put(JSON_KEY_MESSAGE, new JsonObject()
                                                        .put(JSON_KEY_MESSAGE_ID, this.getChatMessage().getId())
                                                        .put(JSON_KEY_MESSAGE_STATUS, ChatMessageStatus.ANSWERED.toString().toLowerCase())
                                                        .put(JSON_KEY_MESSAGE_ACTION, ChatMessageAction.MARK_READ.toString()))
                                                .put(JSON_KEY_FIELDS, new JsonArray()
                                                        .add(JSON_KEY_FIELD_STATUS)
                                                        .add(JSON_KEY_FIELD_ACTION)));

                SlideshowFXServer.getSingleton().callService(request.encode());
            }
        });

        this.chatMessage.addListener((value, oldMessage, newMessage) -> {
            if(newMessage != null) {
                this.authorLabel.setText(newMessage.getAuthor() + " said:");
                this.messageContent.setText(newMessage.getContent());

                if(newMessage.getStatus() == ChatMessageStatus.ANSWERED) {
                    this.pseudoClassStateChanged(this.answeredPseudoClass, true);
                    this.authorLabel.pseudoClassStateChanged(this.answeredPseudoClass, true);
                    this.messageContent.pseudoClassStateChanged(this.answeredPseudoClass, true);
                }

                this.layoutChildren();
            }
        });

        this.authorLabel.getStyleClass().add("message-author");
        this.authorLabel.wrappingWidthProperty().bind(this.widthProperty().subtract(20));
        this.authorLabel.setLayoutX(5);

        this.messageContent.getStyleClass().add("message-content");
        this.messageContent.wrappingWidthProperty().bind(this.widthProperty().subtract(20));
        this.messageContent.setX(15);

        this.setPadding(new Insets(5, 5, 5, 5));
        this.getChildren().addAll(this.authorLabel, this.messageContent);
    }

    /**
     * Get the {@link ChatMessage} associated to this component.
     * @return The ChatMessage associated to this component.
     */
    public ObjectProperty<ChatMessage> chatMessageProperty() { return chatMessage; }

    /**
     * Get the ChatMessage associated to this component.
     * @return The ChatMessage associated to this component.
     */
    public ChatMessage getChatMessage() { return chatMessage.get(); }

    /**
     * Set the ChatMessage associated to this component.
     * @param chatMessage The ChatMessage associated to this component.
     */
    public void setChatMessage(ChatMessage chatMessage) { this.chatMessage.set(chatMessage); }
}
