package com.saltclient.gui;

import com.saltclient.audio.SongPlayerService;
import com.saltclient.util.UiFonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class SongPlayerScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int PANEL = 0xE0131A27;
    private static final int PANEL_BORDER = 0xFF23324A;
    private static final int TEXT = 0xFFE6EBFA;
    private static final int MUTED = 0xFF8EA1C8;
    private static final int POSITIVE = 0xFF8DE39F;
    private static final int NEGATIVE = 0xFFFF8A8A;

    private final Screen parent;

    private final List<Path> tracks = new ArrayList<>();
    private int selected = -1;
    private double scroll;
    private long lastClickMs;
    private int lastClickIndex = -1;

    private String status = "Drop .mp3/.ogg files into the music folder";
    private int statusColor = MUTED;

    public SongPlayerScreen(Screen parent) {
        super(UiFonts.text("Song Player"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip default blur.
    }

    @Override
    protected void init() {
        refreshTracks();

        int panelW = Math.min(this.width - 40, 640);
        int panelH = Math.min(this.height - 40, 360);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Refresh"), b -> refreshTracks())
            .dimensions(panelX + 20, panelY + panelH - 30, 90, 20)
            .build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Play"), b -> playSelected())
            .dimensions(panelX + 118, panelY + panelH - 30, 80, 20)
            .build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Stop"), b -> {
            SongPlayerService.stop();
            setStatus("Stopped", MUTED);
        }).dimensions(panelX + 206, panelY + panelH - 30, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Spotify"), b -> {
            if (this.client != null) this.client.setScreen(new SpotifyScreen(this));
        }).dimensions(panelX + 294, panelY + panelH - 30, 90, 20).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Back"), b -> close())
            .dimensions(panelX + panelW - 100, panelY + panelH - 30, 80, 20)
            .build());
    }

    private void refreshTracks() {
        SongPlayerService.ensureMusicDir();
        tracks.clear();
        tracks.addAll(SongPlayerService.listTracks());

        if (tracks.isEmpty()) {
            selected = -1;
            setStatus("No tracks found in: " + SongPlayerService.musicDir(), MUTED);
        } else {
            if (selected >= tracks.size()) selected = tracks.size() - 1;
            if (selected < 0) selected = 0;
            setStatus("Found " + tracks.size() + " track(s)", POSITIVE);
        }
    }

    private void playSelected() {
        if (selected < 0 || selected >= tracks.size()) {
            setStatus("Select a track first", NEGATIVE);
            return;
        }
        Path track = tracks.get(selected);
        String result = SongPlayerService.play(track);
        boolean ok = result.startsWith("Playing:");
        setStatus(result, ok ? POSITIVE : NEGATIVE);
    }

    private void setStatus(String text, int color) {
        this.status = text == null ? "" : text;
        this.statusColor = color;
    }

    @Override
    public void close() {
        if (this.client != null && parent != null) {
            this.client.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int panelW = Math.min(this.width - 40, 640);
        int panelH = Math.min(this.height - 40, 360);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int listX = panelX + 20;
        int listY = panelY + 70;
        int listW = panelW - 40;
        int listH = panelH - 130;

        if (inside(mouseX, mouseY, listX, listY, listW, listH)) {
            int rowH = 22;
            int idx = (int) ((mouseY - listY + scroll) / rowH);
            if (idx >= 0 && idx < tracks.size()) {
                selected = idx;
                long now = System.currentTimeMillis();
                if (idx == lastClickIndex && now - lastClickMs <= 320L) {
                    playSelected();
                }
                lastClickIndex = idx;
                lastClickMs = now;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = Math.min(this.width - 40, 640);
        int panelH = Math.min(this.height - 40, 360);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int listX = panelX + 20;
        int listY = panelY + 70;
        int listW = panelW - 40;
        int listH = panelH - 130;

        if (inside(mouseX, mouseY, listX, listY, listW, listH)) {
            int rowH = 22;
            int maxScroll = Math.max(0, tracks.size() * rowH - listH);
            scroll -= verticalAmount * 22.0;
            if (scroll < 0.0) scroll = 0.0;
            if (scroll > maxScroll) scroll = maxScroll;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 640);
        int panelH = Math.min(this.height - 40, 360);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text("Song Player"), this.width / 2, panelY + 14, TEXT);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Music folder: " + SongPlayerService.musicDir()), panelX + 20, panelY + 34, MUTED);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Supports .mp3 and .ogg files"), panelX + 20, panelY + 48, MUTED);

        int listX = panelX + 20;
        int listY = panelY + 70;
        int listW = panelW - 40;
        int listH = panelH - 130;
        ctx.fill(listX, listY, listX + listW, listY + listH, 0x66131A2D);
        ctx.drawBorder(listX, listY, listW, listH, 0xFF334568);

        int rowH = 22;
        int visibleRows = Math.max(1, listH / rowH);
        int maxScroll = Math.max(0, tracks.size() * rowH - listH);
        if (scroll > maxScroll) scroll = maxScroll;

        int startIndex = (int) (scroll / rowH);
        int yOffset = (int) (scroll % rowH);

        for (int row = 0; row <= visibleRows; row++) {
            int idx = startIndex + row;
            if (idx >= tracks.size()) break;

            int y = listY + row * rowH - yOffset;
            if (y + rowH <= listY || y >= listY + listH) continue;

            Path track = tracks.get(idx);
            boolean hover = inside(mouseX, mouseY, listX + 2, y, listW - 4, rowH);
            boolean selectedRow = idx == selected;

            int rowColor;
            if (selectedRow) rowColor = 0xAA35518A;
            else rowColor = hover ? 0x66304A78 : 0x3322304A;

            ctx.fill(listX + 2, y, listX + listW - 2, y + rowH, rowColor);
            ctx.drawTextWithShadow(this.textRenderer, UiFonts.text(track.getFileName().toString()), listX + 10, y + 7, TEXT);
        }

        if (tracks.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text("No tracks found"), listX + listW / 2, listY + listH / 2 - 4, MUTED);
        }

        Path current = SongPlayerService.currentTrack();
        if (SongPlayerService.isPlaying() && current != null) {
            ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Now playing: " + current.getFileName()), panelX + 20, panelY + panelH - 46, POSITIVE);
        }

        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text(status), panelX + 20, panelY + panelH - 60, statusColor);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
