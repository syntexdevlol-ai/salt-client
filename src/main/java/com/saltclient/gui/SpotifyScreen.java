package com.saltclient.gui;

import com.saltclient.spotify.SpotifyAuthStore;
import com.saltclient.spotify.SpotifyOAuth;
import com.saltclient.spotify.SpotifyWebApi;
import com.saltclient.util.UiFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Spotify Web API remote control screen.
 *
 * Note: Spotify does not provide raw audio streaming via the Web API. This screen controls
 * playback on your active Spotify device (Spotify Connect).
 */
public final class SpotifyScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int PANEL = 0xE0131A27;
    private static final int PANEL_BORDER = 0xFF23324A;
    private static final int TEXT = 0xFFE6EBFA;
    private static final int MUTED = 0xFF8EA1C8;
    private static final int POSITIVE = 0xFF8DE39F;
    private static final int NEGATIVE = 0xFFFF8A8A;

    private final Screen parent;

    private TextFieldWidget clientIdField;
    private TextFieldWidget searchField;

    private SpotifyWebApi.NowPlaying nowPlaying;
    private final List<SpotifyWebApi.Track> results = new ArrayList<>();

    private double scroll;
    private int selected = -1;

    private String status = "Spotify login required";
    private int statusColor = MUTED;

    public SpotifyScreen(Screen parent) {
        super(UiFonts.text("Spotify"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip default blur.
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 40, 740);
        int panelH = Math.min(this.height - 40, 440);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int x = panelX + 20;
        int y = panelY + 54;

        // Client ID (Spotify Developer app)
        SpotifyAuthStore.Auth auth = SpotifyAuthStore.load();
        clientIdField = new TextFieldWidget(this.textRenderer, x, y, panelW - 40 - 268, 18, Text.literal(""));
        clientIdField.setMaxLength(64);
        clientIdField.setText(auth.clientId());
        clientIdField.setDrawsBackground(true);
        clientIdField.setEditableColor(TEXT);
        clientIdField.setUneditableColor(MUTED);
        this.addDrawableChild(clientIdField);

        if (!auth.accessToken().isBlank() || !auth.refreshToken().isBlank()) {
            setStatus("Logged in", POSITIVE);
        } else {
            setStatus("Login: set redirect URI to " + SpotifyOAuth.REDIRECT_URI, MUTED);
        }

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Save"), b -> saveClientId())
            .dimensions(x + panelW - 40 - 258, y, 60, 18)
            .build());
        addDrawableChild(ButtonWidget.builder(UiFonts.text("Login"), b -> login())
            .dimensions(x + panelW - 40 - 192, y, 60, 18)
            .build());
        addDrawableChild(ButtonWidget.builder(UiFonts.text("Logout"), b -> logout())
            .dimensions(x + panelW - 40 - 126, y, 60, 18)
            .build());
        addDrawableChild(ButtonWidget.builder(UiFonts.text("Now"), b -> refreshNowPlaying())
            .dimensions(x + panelW - 40 - 58, y, 58, 18)
            .build());

        // Controls
        int controlsY = y + 46;
        addDrawableChild(ButtonWidget.builder(Text.literal("<<"), b -> prev())
            .dimensions(x, controlsY, 52, 18)
            .build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> playPause())
            .dimensions(x + 60, controlsY, 52, 18)
            .build());
        addDrawableChild(ButtonWidget.builder(Text.literal(">>"), b -> next())
            .dimensions(x + 120, controlsY, 52, 18)
            .build());

        // Search
        int searchY = controlsY + 60;
        searchField = new TextFieldWidget(this.textRenderer, x, searchY, panelW - 40 - 78, 18, Text.literal(""));
        searchField.setMaxLength(120);
        searchField.setDrawsBackground(true);
        searchField.setEditableColor(TEXT);
        searchField.setUneditableColor(MUTED);
        this.addDrawableChild(searchField);

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Search"), b -> search())
            .dimensions(x + panelW - 40 - 70, searchY, 70, 18)
            .build());

        // Back
        addDrawableChild(ButtonWidget.builder(UiFonts.text("Back"), b -> close())
            .dimensions(panelX + panelW - 100, panelY + panelH - 30, 80, 20)
            .build());
    }

    private String clientId() {
        return clientIdField == null ? "" : clientIdField.getText().trim();
    }

    private void saveClientId() {
        String id = clientId();
        if (id.isEmpty()) {
            setStatus("Client ID is empty", NEGATIVE);
            return;
        }
        boolean ok = SpotifyAuthStore.saveClientId(id);
        setStatus(ok ? "Client ID saved" : "Save failed", ok ? POSITIVE : NEGATIVE);
    }

    private void login() {
        String id = clientId();
        if (id.isEmpty()) {
            setStatus("Paste your Spotify Client ID first", NEGATIVE);
            return;
        }
        if (SpotifyOAuth.isLoginInProgress()) {
            setStatus("Login already in progress", MUTED);
            return;
        }

        setStatus("Starting Spotify login...", MUTED);
        SpotifyOAuth.startLogin(id, new SpotifyOAuth.Listener() {
            @Override
            public void info(String msg) {
                setStatus(msg, MUTED);
            }

            @Override
            public void success(String msg) {
                setStatus(msg, POSITIVE);
            }

            @Override
            public void error(String msg) {
                setStatus(msg, NEGATIVE);
            }

            @Override
            public void onLoggedIn() {
                refreshNowPlaying();
            }
        });
    }

    private void logout() {
        SpotifyAuthStore.clearTokensKeepClientId();
        nowPlaying = null;
        results.clear();
        selected = -1;
        scroll = 0.0;
        setStatus("Logged out", MUTED);
    }

    private void refreshNowPlaying() {
        setStatus("Fetching now playing...", MUTED);
        runAsync(() -> {
            String t = SpotifyOAuth.getValidAccessTokenBlocking();
            if (t.isEmpty()) throw new IllegalStateException("Not logged in");

            SpotifyWebApi.NowPlaying np = SpotifyWebApi.getNowPlaying(t);
            onMain(() -> {
                nowPlaying = np;
                if (np == null) setStatus("Nothing playing (or no active device)", MUTED);
                else setStatus("Now playing updated", POSITIVE);
            });
        });
    }

    private void playPause() {
        boolean playing = nowPlaying != null && nowPlaying.isPlaying();
        setStatus(playing ? "Pausing..." : "Resuming...", MUTED);
        runAsync(() -> {
            String t = SpotifyOAuth.getValidAccessTokenBlocking();
            if (t.isEmpty()) throw new IllegalStateException("Not logged in");

            if (playing) SpotifyWebApi.pause(t);
            else SpotifyWebApi.resume(t);
            SpotifyWebApi.NowPlaying np = SpotifyWebApi.getNowPlaying(t);
            onMain(() -> {
                nowPlaying = np;
                setStatus("OK", POSITIVE);
            });
        });
    }

    private void next() {
        setStatus("Next...", MUTED);
        runAsync(() -> {
            String t = SpotifyOAuth.getValidAccessTokenBlocking();
            if (t.isEmpty()) throw new IllegalStateException("Not logged in");

            SpotifyWebApi.next(t);
            SpotifyWebApi.NowPlaying np = SpotifyWebApi.getNowPlaying(t);
            onMain(() -> {
                nowPlaying = np;
                setStatus("OK", POSITIVE);
            });
        });
    }

    private void prev() {
        setStatus("Previous...", MUTED);
        runAsync(() -> {
            String t = SpotifyOAuth.getValidAccessTokenBlocking();
            if (t.isEmpty()) throw new IllegalStateException("Not logged in");

            SpotifyWebApi.previous(t);
            SpotifyWebApi.NowPlaying np = SpotifyWebApi.getNowPlaying(t);
            onMain(() -> {
                nowPlaying = np;
                setStatus("OK", POSITIVE);
            });
        });
    }

    private void search() {
        String q = searchField == null ? "" : searchField.getText().trim();
        if (q.isEmpty()) {
            setStatus("Type a search query", NEGATIVE);
            return;
        }

        setStatus("Searching...", MUTED);
        runAsync(() -> {
            String t = SpotifyOAuth.getValidAccessTokenBlocking();
            if (t.isEmpty()) throw new IllegalStateException("Not logged in");

            List<SpotifyWebApi.Track> found = SpotifyWebApi.searchTracks(t, q, 25);
            onMain(() -> {
                results.clear();
                results.addAll(found);
                selected = results.isEmpty() ? -1 : 0;
                scroll = 0.0;
                setStatus("Found " + results.size() + " track(s)", POSITIVE);
            });
        });
    }

    private void playSelected() {
        if (selected < 0 || selected >= results.size()) return;

        SpotifyWebApi.Track track = results.get(selected);
        setStatus("Playing via Spotify...", MUTED);
        runAsync(() -> {
            String t = SpotifyOAuth.getValidAccessTokenBlocking();
            if (t.isEmpty()) throw new IllegalStateException("Not logged in");

            SpotifyWebApi.playTrack(t, track.uri());
            SpotifyWebApi.NowPlaying np = SpotifyWebApi.getNowPlaying(t);
            onMain(() -> {
                nowPlaying = np;
                setStatus("OK", POSITIVE);
            });
        });
    }

    private void setStatus(String text, int color) {
        this.status = text == null ? "" : text;
        this.statusColor = color;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private void runAsync(ThrowingRunnable work) {
        new Thread(() -> {
            try {
                work.run();
            } catch (Exception e) {
                Throwable t = e;
                // Unwrap common wrapper exception shapes.
                if (t.getCause() != null && (t instanceof RuntimeException)) t = t.getCause();

                String msg = t.getMessage();
                if (msg == null || msg.isBlank()) msg = t.getClass().getSimpleName();
                String finalMsg = msg;
                onMain(() -> setStatus("Error: " + finalMsg, NEGATIVE));
            }
        }, "salt-spotify").start();
    }

    private void onMain(Runnable r) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) mc.execute(r);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = Math.min(this.width - 40, 740);
        int panelH = Math.min(this.height - 40, 440);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int listX = panelX + 20;
        int listY = panelY + 200;
        int listW = panelW - 40;
        int listH = panelH - 260;

        if (inside(mouseX, mouseY, listX, listY, listW, listH)) {
            int rowH = 22;
            int idx = (int) ((mouseY - listY + scroll) / rowH);
            if (idx >= 0 && idx < results.size()) {
                selected = idx;
                if (button == 0) playSelected();
                return true;
            }
        }

        if (clientIdField != null && clientIdField.mouseClicked(mouseX, mouseY, button)) return true;
        if (searchField != null && searchField.mouseClicked(mouseX, mouseY, button)) return true;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (clientIdField != null && clientIdField.charTyped(chr, modifiers)) return true;
        if (searchField != null && searchField.charTyped(chr, modifiers)) return true;
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (clientIdField != null && clientIdField.keyPressed(keyCode, scanCode, modifiers)) return true;
        if (searchField != null && searchField.keyPressed(keyCode, scanCode, modifiers)) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = Math.min(this.width - 40, 740);
        int panelH = Math.min(this.height - 40, 440);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int listX = panelX + 20;
        int listY = panelY + 200;
        int listW = panelW - 40;
        int listH = panelH - 260;

        if (inside(mouseX, mouseY, listX, listY, listW, listH)) {
            int rowH = 22;
            int maxScroll = Math.max(0, results.size() * rowH - listH);
            scroll -= verticalAmount * 22.0;
            if (scroll < 0.0) scroll = 0.0;
            if (scroll > maxScroll) scroll = maxScroll;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void close() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && parent != null) {
            mc.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 740);
        int panelH = Math.min(this.height - 40, 440);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;
        int baseY = panelY + 54;
        int controlsY = baseY + 46;
        int nowY = controlsY + 24;
        int searchLabelY = controlsY + 48;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 14, TEXT);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Spotify remote control (plays on your active Spotify device)"), panelX + 20, panelY + 34, MUTED);

        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Client ID (Spotify Developer app)"), panelX + 20, panelY + 54 - 12, MUTED);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Redirect URI: " + SpotifyOAuth.REDIRECT_URI), panelX + 20, panelY + 54 + 20, MUTED);

        // Text fields
        if (clientIdField != null) clientIdField.render(ctx, mouseX, mouseY, delta);
        if (searchField != null) {
            searchField.render(ctx, mouseX, mouseY, delta);
        }

        // Now playing
        String npLine = "Now playing: (unknown)";
        if (nowPlaying == null) {
            npLine = "Now playing: (none)";
        } else {
            String left = nowPlaying.title();
            String right = nowPlaying.artists();
            if (!right.isEmpty()) left = left + " - " + right;
            npLine = "Now playing: " + left + (nowPlaying.isPlaying() ? " [playing]" : " [paused]");
        }
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(npLine), panelX + 20, nowY, TEXT);

        // Search label
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Search Tracks"), panelX + 20, searchLabelY, MUTED);

        // Results list
        int listX = panelX + 20;
        int listY = panelY + 200;
        int listW = panelW - 40;
        int listH = panelH - 260;

        ctx.fill(listX, listY, listX + listW, listY + listH, 0x66131A2D);
        ctx.drawBorder(listX, listY, listW, listH, 0xFF334568);

        int rowH = 22;
        int maxScroll = Math.max(0, results.size() * rowH - listH);
        if (scroll > maxScroll) scroll = maxScroll;

        int startIndex = (int) (scroll / rowH);
        int yOffset = (int) (scroll % rowH);
        int visibleRows = Math.max(1, listH / rowH);

        for (int row = 0; row <= visibleRows; row++) {
            int idx = startIndex + row;
            if (idx >= results.size()) break;

            int y = listY + row * rowH - yOffset;
            if (y + rowH <= listY || y >= listY + listH) continue;

            SpotifyWebApi.Track t = results.get(idx);
            boolean hover = inside(mouseX, mouseY, listX + 2, y, listW - 4, rowH);
            boolean sel = idx == selected;

            int rowColor;
            if (sel) rowColor = 0xAA35518A;
            else rowColor = hover ? 0x66304A78 : 0x3322304A;

            ctx.fill(listX + 2, y, listX + listW - 2, y + rowH, rowColor);

            String line = t.title();
            if (!t.artists().isEmpty()) line += " - " + t.artists();
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(line), listX + 10, y + 7, TEXT);
        }

        if (results.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text("No results"), listX + listW / 2, listY + listH / 2 - 4, MUTED);
        }

        // Status
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text(status), panelX + 20, panelY + panelH - 56, statusColor);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
