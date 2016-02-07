package com.twasyl.slideshowfx.beans.chat;

/**
 * Represents the action supported for a {@link com.twasyl.slideshowfx.beans.chat.ChatMessage}. Currently the only action
 * that is supported is marking a ChatMessage as read.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public enum ChatMessageAction {

    MARK_READ("mark-read");

    private final String asString;

    private ChatMessageAction(String action) { this.asString = action; }

    public String getAsString() { return asString; }

    public static ChatMessageAction fromString(String action) {
       if(MARK_READ.getAsString().equals(action)) {
            return MARK_READ;
        } else {
            return null;
        }
    }
}
