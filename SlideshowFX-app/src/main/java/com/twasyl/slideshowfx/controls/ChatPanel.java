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

package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.beans.chat.ChatMessage;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * This class is the panel that will contain {@link com.twasyl.slideshowfx.controls.ChatBubble}. This panel ensures
 * it's width is the same as the bubbles contained into it. It also provides convenient methods to add chat messages.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since 1.0
 */
public class ChatPanel extends ScrollPane {

    private final double WIDTH = 400;
    private final VBox messages = new VBox(5);

    public ChatPanel() {
        this.getStylesheets().add(getClass().getResource("/com/twasyl/slideshowfx/css/chat-panel.css").toExternalForm());
        this.getStyleClass().add("chat-panel");

        this.setContent(this.messages);
        this.setBackground(null);

        this.setPrefViewportWidth(WIDTH);
        this.setPrefWidth(WIDTH + 10);
        this.setMaxWidth(WIDTH + 10);

        /*
         * Ensure the height of this panel is always the height of the screen
         */
        this.sceneProperty().addListener((sceneValue, oldScene, newScene) -> {
            if(newScene != null) {

                ChatPanel.this.messages.prefHeightProperty().bind(newScene.heightProperty().subtract(10));
                ChatPanel.this.prefHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    /**
     * Add a message to this panel. The message is converted to a ChatBubble and added to the list of
     * children of this panel.
     * @param message The message to add to this panel.
     * @throws java.lang.NullPointerException If the given message is null.
     */
    public synchronized void addMessage(ChatMessage message) {
        if (message == null) throw new NullPointerException("The message to add to the panel can not be null");

        final ChatBubble bubble = new ChatBubble();
        bubble.setChatMessage(message);
        bubble.setPrefWidth(WIDTH);
        bubble.setMinWidth(WIDTH);
        bubble.setMaxWidth(WIDTH);

        this.messages.getChildren().add(bubble);

        this.messages.layout();
    }
}
