package com.twasyl.slideshowfx.server.beans.chat;

/**
 * The possible status of a {@link com.twasyl.slideshowfx.server.beans.chat.ChatMessage}. Currently a message can only be new or
 * answered.
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0
 */
public enum ChatMessageStatus {
    ANSWERED("answered"),
    NEW("new");

    private final String asString;

    ChatMessageStatus(String asString) { this.asString = asString; }

    public String getAsString() { return asString; }

    public static ChatMessageStatus fromString(String asString) {
        if(ANSWERED.getAsString().equals(asString)) {
            return ANSWERED;
        } else if(NEW.getAsString().equals(asString)) {
            return NEW;
        } else {
            return null;
        }
    }
}
