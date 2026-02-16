package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.state.SaltState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Minimal settings for the Zoom module.
 *
 * We keep it simple and Pojav-friendly: just +/- buttons.
 */
public final class ZoomSettingsScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int PANEL = 0xE0131A27;
    private static final int PANEL_BORDER = 0xFF23324A;
    private static final int TEXT = 0xFFE6EBFA;
    private static final int MUTED = 0xFF8EA1C8;

    private final Screen parent;
    private ButtonWidget valueBtn;

    public ZoomSettingsScreen(Screen parent) {
        super(Text.literal("Zoom Settings"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla blur.
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 10;

        int w = 90;
        int h = 20;
        int g = 6;

        addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> {
            SaltState.zoomFov -= 2;
            SaltState.clampZoom();
            SaltClient.CONFIG.save(SaltClient.MODULES);
            refreshValue();
        }).dimensions(cx - 55 - g - w, y, w, h).build());

        valueBtn = addDrawableChild(ButtonWidget.builder(valueText(), b -> {})
            .dimensions(cx - 55, y, 110, h)
            .build());
        valueBtn.active = false;

        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            SaltState.zoomFov += 2;
            SaltState.clampZoom();
            SaltClient.CONFIG.save(SaltClient.MODULES);
            refreshValue();
        }).dimensions(cx + 55 + g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(cx - 60, this.height - 34, 120, 20)
            .build());
    }

    private Text valueText() {
        return Text.literal("Zoom FOV: " + SaltState.zoomFov);
    }

    private void refreshValue() {
        if (valueBtn != null) valueBtn.setMessage(valueText());
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
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 520);
        int panelH = Math.min(this.height - 40, 220);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 16, TEXT);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Lower = more zoom"), this.width / 2, panelY + 36, MUTED);

        super.render(ctx, mouseX, mouseY, delta);
    }
}
