package com.twasyl.slideshowfx.beans.chat;

/**
 * The source of a {@link com.twasyl.slideshowfx.beans.chat.ChatMessage}.
 * Currently the internal chat of SlideshowFX and Twitter can be the source of a ChatMessage
 *
 * @author Thierry Wasylczenko
 * @version 1.0
 * @since SlideshowFX 1.0.0
 */
public enum ChatMessageSource {

    CHAT("chat"),
    TWITTER("twitter");

    private final String asString;

    private ChatMessageSource(String source) { this.asString = source; }

    public String getAsString() {
        return asString;
    }

    public static ChatMessageSource fromString(String action) {
        if(CHAT.getAsString().equals(action)) {
            return CHAT;
        } else if(TWITTER.getAsString().equals(action)) {
            return TWITTER;
        } else {
            return null;
        }
    }
}
