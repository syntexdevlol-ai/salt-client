package com.saltclient.gui;

import com.saltclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * Simple "no settings" / info page for modules.
 *
 * This exists mainly so right-click can open *something* predictable.
 */
public final class ModuleInfoScreen extends Screen {
    private static final int BG = 0xCC12161F;
    private static final int PANEL = 0xDD1A2130;
    private static final int PANEL_BORDER = 0xFF2B3A55;
    private static final int TEXT = 0xFFE6ECFF;
    private static final int MUTED = 0xFF9FB0D8;

    private final Screen parent;
    private final Module module;

    public ModuleInfoScreen(Screen parent, Module module) {
        super(Text.literal(module.getName()));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla blur.
    }

    @Override
    protected void init() {
        int w = 120;
        int h = 20;
        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions((this.width - w) / 2, this.height - 34, w, h)
            .build());
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
        // Background + panel
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 520);
        int panelH = Math.min(this.height - 40, 220);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        // Title + description
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 16, TEXT);

        String desc = module.getDescription() == null ? "" : module.getDescription();
        if (desc.isEmpty()) desc = "No description.";

        // Very small, line-limited wrap (good enough for our short descriptions)
        int maxWidth = panelW - 24;
        int y = panelY + 44;
        for (OrderedText line : this.textRenderer.wrapLines(Text.literal(desc), maxWidth)) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, line, this.width / 2, y, MUTED);
            y += this.textRenderer.fontHeight + 2;
            if (y > panelY + panelH - 60) break;
        }

        ctx.drawCenteredTextWithShadow(
            this.textRenderer,
            Text.literal("No settings for this module yet."),
            this.width / 2,
            panelY + panelH - 60,
            MUTED
        );

        super.render(ctx, mouseX, mouseY, delta);
    }
}
