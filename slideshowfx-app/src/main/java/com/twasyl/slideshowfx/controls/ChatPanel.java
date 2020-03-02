package com.twasyl.slideshowfx.controls;

import com.twasyl.slideshowfx.global.configuration.GlobalConfiguration;
import com.twasyl.slideshowfx.icons.IconStack;
import com.twasyl.slideshowfx.server.beans.chat.ChatMessage;
import com.twasyl.slideshowfx.style.Styles;
import com.twasyl.slideshowfx.style.theme.Themes;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import static com.twasyl.slideshowfx.icons.Icon.BAN;
import static com.twasyl.slideshowfx.icons.Icon.COMMENTS_DOTS;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.TOP_CENTER;

/**
 * This class is the panel that will contain {@link com.twasyl.slideshowfx.controls.ChatBubble}. This panel ensures
 * it's width is the same as the bubbles contained into it. It also provides convenient methods to add chat messages.
 *
 * @author Thierry Wasylczenko
 * @version 1.2-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class ChatPanel extends ScrollPane {

    private final static double WIDTH = 400;
    private final VBox messages = new VBox(5);
    private final IconStack emptyChatStack = new IconStack();

    public ChatPanel() {
        this.getStyleClass().add("chat-panel");
        Styles.applyApplicationStyle(this);
        Themes.applyTheme(this, GlobalConfiguration.getThemeName());

        this.emptyChatStack.getStyleClass().add("empty-chat-stack");
        this.emptyChatStack.setAlignment(CENTER);
        this.emptyChatStack.setPrefWidth(WIDTH);
        this.emptyChatStack.setMinWidth(WIDTH);
        this.emptyChatStack.setMaxWidth(WIDTH);

        this.emptyChatStack
                .addIcon(COMMENTS_DOTS)
                .addIcon(BAN);

        this.messages.setAlignment(CENTER);
        this.messages.getChildren().add(this.emptyChatStack);

        this.setContent(this.messages);

        this.setPrefViewportWidth(WIDTH);
        this.setPrefWidth(WIDTH + 10);
        this.setMaxWidth(WIDTH + 10);

        /*
         * Ensure the height of this panel is always the height of the screen
         */
        this.sceneProperty().addListener((sceneValue, oldScene, newScene) -> {
            if (newScene != null) {
                ChatPanel.this.messages.prefHeightProperty().bind(newScene.heightProperty().subtract(10));
                ChatPanel.this.prefHeightProperty().bind(newScene.heightProperty());
            }
        });
    }

    /**
     * Add a message to this panel. The message is converted to a ChatBubble and added to the list of
     * children of this panel.
     *
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

        if (this.messages.getChildren().size() == 1 && this.messages.getChildren().contains(this.emptyChatStack)) {
            this.messages.getChildren().remove(0);
            this.messages.setAlignment(TOP_CENTER);
        }

        this.messages.getChildren().add(bubble);

        this.messages.layout();
    }
}
