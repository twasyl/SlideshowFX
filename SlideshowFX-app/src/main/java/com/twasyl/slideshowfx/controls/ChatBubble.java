/*
 * Copyright 2015 Thierry Wasylczenko
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

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.beans.chat.ChatMessageAction;
import com.twasyl.slideshowfx.beans.chat.ChatMessageStatus;
import com.twasyl.slideshowfx.server.SlideshowFXServer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

/**
 * This class is used to display a {@link com.twasyl.slideshowfx.beans.chat.ChatMessage} in the {@link com.twasyl.slideshowfx.controls.SlideShowScene}
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
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

                JsonObject request = new JsonObject();
                request.putString("service", "slideshowfx.chat.attendee.message.update")
                        .putObject("data", new JsonObject()
                                                .putObject("message", new JsonObject()
                                                        .putString("id", this.getChatMessage().getId())
                                                        .putString("status", ChatMessageStatus.ANSWERED.toString().toLowerCase())
                                                        .putString("action", ChatMessageAction.MARK_READ.toString()))
                                                .putArray("fields", new JsonArray()
                                                        .addString("status")
                                                        .addString("action")));

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
     * Get the ChatMessage associated to this component.
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
