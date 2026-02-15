package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class MouseButtonsModule extends Module {
    private static final int BG = 0xAA0E121A;
    private static final int BG_ON = 0xFF2B3A55;
    private static final int FG = 0xFFE6ECFF;

    public MouseButtonsModule() {
        super("mousebuttons", "MouseButtons", "Show pressed mouse buttons.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int x = 10;
        int y = HudLayout.nextTopLeft(22);

        int w = 28;
        int h = 18;
        int g = 2;

        drawBtn(ctx, mc.options.attackKey.isPressed(), x, y, w, h, "LMB");
        drawBtn(ctx, mc.options.useKey.isPressed(), x + w + g, y, w, h, "RMB");
    }

    private void drawBtn(DrawContext ctx, boolean pressed, int x, int y, int w, int h, String label) {
        ctx.fill(x, y, x + w, y + h, pressed ? BG_ON : BG);
        int tx = x + (w - MinecraftClient.getInstance().textRenderer.getWidth(label)) / 2;
        int ty = y + (h - MinecraftClient.getInstance().textRenderer.fontHeight) / 2;
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(label), tx, ty, FG);
    }
}
