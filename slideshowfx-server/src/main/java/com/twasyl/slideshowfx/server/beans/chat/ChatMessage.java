package com.twasyl.slideshowfx.server.beans.chat;

import io.vertx.core.json.JsonObject;

import java.net.InetSocketAddress;
import java.util.Base64;

import static com.twasyl.slideshowfx.global.configuration.GlobalConfiguration.getDefaultCharset;

/**
 * This class represents a message that can be sent over the internal chat of SlideshowFX.
 *
 * @author Thierry Wasylczenko
 * @version 1.1-SNAPSHOT
 * @since SlideshowFX 1.0
 */
public class ChatMessage {
    private static final String JSON_MESSAGE_OBJECT = "message";
    private static final String JSON_MESSAGE_ID_ATTR = "id";
    private static final String JSON_MESSAGE_AUTHOR_ATTR = "author";
    private static final String JSON_MESSAGE_CONTENT_ATTR = "content";
    private static final String JSON_MESSAGE_ACTION_ATTR = "action";
    private static final String JSON_MESSAGE_SOURCE_ATTR = "source";
    private static final String JSON_MESSAGE_STATUS_ATTR = "status";

    private String id;
    private String author;
    private String content;
    private ChatMessageSource source;
    private ChatMessageAction action;
    private ChatMessageStatus status;
    private InetSocketAddress ip;

    public String getId() { return id; }
    public void setId(String id) {
        if(id != null) {
            if(id.startsWith("msg-")) this.id = id;
            else this.id = "msg-".concat(id);
        }
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public ChatMessageSource getSource() { return source; }
    public void setSource(ChatMessageSource source) { this.source = source; }

    public ChatMessageAction getAction() { return action; }
    public void setAction(ChatMessageAction action) { this.action = action; }

    public ChatMessageStatus getStatus() { return status; }
    public void setStatus(ChatMessageStatus status) { this.status = status; }

    public InetSocketAddress getIp() { return ip; }
    public void setIp(InetSocketAddress ip) { this.ip = ip; }

    /**
     * Encode the content in Base64.
     * @return the content of the message encoded in Base64.
     */
    private String encodeContent() {
        if(getContent() != null) {
            return Base64.getEncoder().encodeToString(getContent().getBytes(getDefaultCharset()));
        }
        return null;
    }

    /**
     * Decode the given base64 content.
     * @param contentToDecode The content that must be decoded.
     * @return The decoded content.
     */
    private String decodeContent(String contentToDecode) {
        if(contentToDecode != null) {
            return new String(Base64.getDecoder().decode(contentToDecode), getDefaultCharset());
        }
        return null;
    }

    /**
     * Build a ChatMessage according the JSON representation.
     * @param json The JSON representation of the message.
     * @param ip The IP address of the message.
     * @return A ChatMessage according the JSON representation.
     * @throws IllegalArgumentException If the JSON representation is {@code null} or empty.
     */
    public static ChatMessage build(String json, InetSocketAddress ip) {
        if(json == null) throw new IllegalArgumentException("The JSON can not be null");
        if(json.isEmpty()) throw new IllegalArgumentException("The JSON can not beb empty");

        ChatMessage message = new ChatMessage();
        message.setIp(ip);

        JsonObject jsonObject = new JsonObject(json);

        message.setId(jsonObject.getString(JSON_MESSAGE_ID_ATTR));
        message.setAuthor(jsonObject.getString(JSON_MESSAGE_AUTHOR_ATTR));
        message.setSource(ChatMessageSource.fromString(jsonObject.getString(JSON_MESSAGE_SOURCE_ATTR)));
        message.setAction(ChatMessageAction.fromString(jsonObject.getString(JSON_MESSAGE_ACTION_ATTR)));
        message.setStatus(ChatMessageStatus.fromString(jsonObject.getString(JSON_MESSAGE_STATUS_ATTR)));
        message.setContent(message.decodeContent(jsonObject.getString(JSON_MESSAGE_CONTENT_ATTR)));

        return message;
    }

    /**
     * Build the JSON representation of this ChatMessage.
     *
     * @return The JSON representation of this ChatMessage
     */
    public JsonObject toJSON() { return getJSONObject(); }

    /**
     * Build the JSON representation of this ChatMessage. The given <code>ip</code> is used to determine who is the
     * author of this message. If the IP of the message is equal to the given <code>ip</code> then the author is
     * identified as <code>I</code> in the JSON representation.
     *
     * @param ip The IP address used to determine the author of this message.
     * @return The JSON representation of this ChatMessage.
     */
    public JsonObject toJSON(InetSocketAddress ip) {
        JsonObject jsonObject = getJSONObject();

        if(this.getIp() != null && this.getIp().equals(ip)) {
            jsonObject.getJsonObject(JSON_MESSAGE_OBJECT).put(JSON_MESSAGE_AUTHOR_ATTR, "I");
        }

        return jsonObject;
    }

    /**
     * Build the JsonObject associated to this ChatMessage. Only attributes of this ChatMessage that are not null are
     * inserted in the JsonObject.
     *
     * @return The JSON object representing this ChatMessage.
     */
    private JsonObject getJSONObject() {
        JsonObject jsonMessage = new JsonObject();

        if(getId() != null) jsonMessage.put(JSON_MESSAGE_ID_ATTR, getId());

        if(getAuthor() != null) jsonMessage.put(JSON_MESSAGE_AUTHOR_ATTR, getAuthor());

        if(getContent() != null) jsonMessage.put(JSON_MESSAGE_CONTENT_ATTR, encodeContent());

        if(getSource() != null) jsonMessage.put(JSON_MESSAGE_SOURCE_ATTR, getSource().getAsString());

        if(getAction() != null) jsonMessage.put(JSON_MESSAGE_ACTION_ATTR, getAction().getAsString());

        if(getStatus() != null) jsonMessage.put(JSON_MESSAGE_STATUS_ATTR, getStatus().getAsString());

        return jsonMessage;
    }
}
