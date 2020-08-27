package com.del.delcontainer.utils.chatBotUtils;

public class ChatMessage {

    String text;
    boolean user;

    public ChatMessage(String text, boolean user) {
        this.text = text;
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean user() {
        return user;
    }

    public void setUser(boolean sender) {
        user = sender;
    }
}
