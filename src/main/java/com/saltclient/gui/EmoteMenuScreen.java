package com.saltclient.gui;

import com.saltclient.util.UiFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Small "emote menu" screen: click an emote button to send it in chat.
 *
 * Kept intentionally lightweight (no fancy rendering) so it stays stable on Pojav/FCL.
 */
public final class EmoteMenuScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int PANEL = 0xE0131A27;
    private static final int PANEL_BORDER = 0xFF23324A;
    private static final int TEXT_COL = 0xFFE6EBFA;

    private final Screen parent;

    // A small default set; users can still type anything manually.
    private static final String[] EMOTES = {
        "gg", "wp", "ez", "o/",
        "<3", ":)", ":D", "xd",
        "rip", "ty", "brb", "lol"
    };

    public EmoteMenuScreen(Screen parent) {
        super(UiFonts.text("Emote Menu"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla blur.
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 40, 480);
        int panelH = Math.min(this.height - 40, 280);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int cols = 4;
        int gap = 8;
        int btnH = 22;
        int btnW = (panelW - 40 - (cols - 1) * gap) / cols;

        int startX = panelX + 20;
        int startY = panelY + 54;

        for (int i = 0; i < EMOTES.length; i++) {
            String emote = EMOTES[i];
            int row = i / cols;
            int col = i % cols;
            int x = startX + col * (btnW + gap);
            int y = startY + row * (btnH + gap);

            addDrawableChild(ButtonWidget.builder(Text.literal(emote), b -> sendEmote(emote))
                .dimensions(x, y, btnW, btnH)
                .build());
        }

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Back"), b -> close())
            .dimensions(panelX + panelW - 100, panelY + panelH - 30, 80, 20)
            .build());
    }

    private void sendEmote(String emote) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendChatMessage(emote);
        }
        close();
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

        int panelW = Math.min(this.width - 40, 480);
        int panelH = Math.min(this.height - 40, 280);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 16, TEXT_COL);
        ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text("Click an emote to send it in chat"), this.width / 2, panelY + 34, 0xFF8EA1C8);

        super.render(ctx, mouseX, mouseY, delta);
    }
}

