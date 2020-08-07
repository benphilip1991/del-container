package com.del.delcontainer.ui.chatbot;

public class ChatType {

    String text;
    boolean user;

    public ChatType(String text, boolean user) {
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
