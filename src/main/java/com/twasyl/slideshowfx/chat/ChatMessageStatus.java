package com.twasyl.slideshowfx.chat;

public enum ChatMessageStatus {
    ANSWERED("answered"),
    NEW("new");

    private String asString;

    private ChatMessageStatus(String asString) { this.asString = asString; }

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
