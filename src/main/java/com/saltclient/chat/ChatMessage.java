package com.saltclient.chat;

import java.time.Instant;

public final class ChatMessage {
    public final String user;
    public final String text;
    public final Instant time;

    public ChatMessage(String user, String text) {
        this.user = user == null ? "unknown" : user;
        this.text = text == null ? "" : text;
        this.time = Instant.now();
    }
}
