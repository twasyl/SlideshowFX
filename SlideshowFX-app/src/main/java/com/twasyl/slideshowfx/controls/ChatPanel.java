package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.utils.ResourceHelper;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * This class is the panel that will contain {@link com.twasyl.slideshowfx.controls.ChatBubble}. This panel ensures
 * it's width is the same as the bubbles contained into it. It also provides convenient methods to add chat messages.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public class ChatPanel extends ScrollPane {

    private final static double WIDTH = 400;
    private final VBox messages = new VBox(5);

    public ChatPanel() {
        this.getStylesheets().add(ResourceHelper.getExternalForm("/com/twasyl/slideshowfx/css/chat-panel.css"));
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
