package com.twasyl.slideshowfx.chat;

public enum ChatMessageAction {

    MARK_READ("mark-read");

    private String asString;

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
