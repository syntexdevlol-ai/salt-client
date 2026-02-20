package com.saltclient.chat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Minimal WebSocket client for global chat.
 *
 * Default endpoint can be overridden with JVM prop: -Dsaltclient.chat.url=wss://...
 * Uses best-effort connection; failures are swallowed so the client remains playable.
 */
public final class ChatService implements WebSocket.Listener {
    private static final Gson GSON = new Gson();
    private static final int MAX_MESSAGES = 200;
    private static final URI DEFAULT_URI = URI.create(System.getProperty(
        "saltclient.chat.url",
        "wss://echo.websocket.events" // harmless public echo; replace with real backend
    ));

    public static final ChatService INSTANCE = new ChatService();

    private final Queue<ChatMessage> messages = new ArrayDeque<>();
    private final HttpClient client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    private WebSocket socket;
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private String username = "user";
    private Consumer<Void> onUpdate = v -> {};

    private ChatService() {}

    public synchronized List<ChatMessage> snapshot() {
        return List.copyOf(messages);
    }

    public void setOnUpdate(Consumer<Void> listener) {
        this.onUpdate = listener == null ? v -> {} : listener;
    }

    public void ensureConnected() {
        if (socket != null && socket.isInputClosed()) {
            socket = null;
        }
        if (socket != null || connecting.get()) return;

        connecting.set(true);
        try {
            String user = MinecraftClient.getInstance().getSession().getUsername();
            this.username = user == null ? "user" : user;
        } catch (Exception ignored) {}

        client.newWebSocketBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .buildAsync(DEFAULT_URI, this)
            .whenComplete((ws, err) -> {
                connecting.set(false);
                if (err != null) {
                    socket = null;
                } else {
                    socket = ws;
                    sendSystem("Connected to global chat");
                }
            });
    }

    public void disconnect() {
        if (socket != null) {
            socket.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
            socket = null;
        }
    }

    public void send(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) return;
        ensureConnected();
        addMessage(new ChatMessage(username, trimmed));
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("user", username);
            obj.addProperty("text", trimmed);
            String payload = GSON.toJson(obj);
            if (socket != null) socket.sendText(payload, true);
        } catch (Exception ignored) {}
    }

    private void sendSystem(String text) {
        addMessage(new ChatMessage("system", text));
    }

    private synchronized void addMessage(ChatMessage msg) {
        messages.add(msg);
        while (messages.size() > MAX_MESSAGES) messages.poll();
        onUpdate.accept(null);
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try {
            JsonObject obj = GSON.fromJson(data.toString(), JsonObject.class);
            if (obj != null && obj.has("text")) {
                String user = obj.has("user") ? obj.get("user").getAsString() : "user";
                String text = obj.get("text").getAsString();
                addMessage(new ChatMessage(user, text));
            } else {
                addMessage(new ChatMessage("remote", data.toString()));
            }
        } catch (Exception ignored) {
            addMessage(new ChatMessage("remote", data.toString()));
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        addMessage(new ChatMessage("system", "Chat error: " + error.getMessage()));
    }
}
