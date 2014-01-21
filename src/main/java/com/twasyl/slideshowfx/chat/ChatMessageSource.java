package com.twasyl.slideshowfx.chat;

public enum ChatMessageSource {

    CHAT("chat"),
    TWITTER("twitter");

    private String asString;

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
