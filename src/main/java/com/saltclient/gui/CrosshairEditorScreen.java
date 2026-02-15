package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.state.SaltState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Minimal crosshair editor (lite).
 *
 * This is intentionally simple: a few +/- buttons and a color cycle.
 */
public final class CrosshairEditorScreen extends Screen {
    private static final int[] COLORS = new int[] {
        0xFFE6ECFF, // white-ish
        0xFF7BC96F, // green
        0xFFFF5C5C, // red
        0xFF8BE0FF, // cyan
        0xFFFFD66E  // yellow
    };

    private final Screen parent;

    public CrosshairEditorScreen() {
        this(null);
    }

    public CrosshairEditorScreen(Screen parent) {
        super(Text.literal("Crosshair Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla background blur (Pojav-friendly).
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
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 + 50;

        int w = 90;
        int h = 18;
        int g = 6;

        addDrawableChild(ButtonWidget.builder(Text.literal("Size -"), b -> {
            SaltState.crosshairSize--;
            SaltState.clampCrosshair();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Size +"), b -> {
            SaltState.crosshairSize++;
            SaltState.clampCrosshair();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(Text.literal("Gap -"), b -> {
            SaltState.crosshairGap--;
            SaltState.clampCrosshair();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Gap +"), b -> {
            SaltState.crosshairGap++;
            SaltState.clampCrosshair();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(Text.literal("Thick -"), b -> {
            SaltState.crosshairThickness--;
            SaltState.clampCrosshair();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Thick +"), b -> {
            SaltState.crosshairThickness++;
            SaltState.clampCrosshair();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(Text.literal("Dot: " + (SaltState.crosshairDot ? "ON" : "OFF")), b -> {
            SaltState.crosshairDot = !SaltState.crosshairDot;
            b.setMessage(Text.literal("Dot: " + (SaltState.crosshairDot ? "ON" : "OFF")));
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w * 2 + g * 2, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(Text.literal("Color"), b -> {
            int idx = 0;
            for (int i = 0; i < COLORS.length; i++) {
                if (COLORS[i] == SaltState.crosshairColor) {
                    idx = i;
                    break;
                }
            }
            SaltState.crosshairColor = COLORS[(idx + 1) % COLORS.length];
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w * 2 + g * 2, h).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Don't call Screen#renderBackground here; on some clients (e.g. Pojav) it can enable expensive blur.
        ctx.fill(0, 0, this.width, this.height, 0xCC12161F);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFE6ECFF);

        // Crosshair preview
        int cx = this.width / 2;
        int cy = this.height / 2;
        drawCrosshair(ctx, cx, cy);

        String info = "Size=" + SaltState.crosshairSize
            + " Gap=" + SaltState.crosshairGap
            + " Thick=" + SaltState.crosshairThickness
            + " Dot=" + (SaltState.crosshairDot ? "ON" : "OFF");
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(info), cx, cy + 22, 0xFF9FB0D8);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private static void drawCrosshair(DrawContext ctx, int cx, int cy) {
        int size = SaltState.crosshairSize;
        int gap = SaltState.crosshairGap;
        int t = SaltState.crosshairThickness;
        int c = SaltState.crosshairColor;

        // Horizontal
        ctx.fill(cx - gap - size, cy - t / 2, cx - gap, cy + (t + 1) / 2, c);
        ctx.fill(cx + gap, cy - t / 2, cx + gap + size, cy + (t + 1) / 2, c);
        // Vertical
        ctx.fill(cx - t / 2, cy - gap - size, cx + (t + 1) / 2, cy - gap, c);
        ctx.fill(cx - t / 2, cy + gap, cx + (t + 1) / 2, cy + gap + size, c);

        if (SaltState.crosshairDot) {
            ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
        }
    }
}
