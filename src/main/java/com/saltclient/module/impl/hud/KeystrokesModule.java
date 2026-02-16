package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class KeystrokesModule extends Module {
    private static final int BG = 0xAA0E121A;
    private static final int BG_ON = 0xFF2B3A55;
    private static final int FG = 0xFFE6ECFF;

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", "Show pressed movement keys.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int defaultX = 10;
        int defaultY = HudLayout.nextTopLeft(44);
        HudPos.Pos pos = HudPos.resolve("keystrokes", defaultX, defaultY);
        int x = pos.x;
        int y = pos.y;

        int s = 18;
        int g = 2;

        // W
        drawKey(ctx, mc.options.forwardKey.isPressed(), x + s + g, y, s, "W");
        // A S D
        drawKey(ctx, mc.options.leftKey.isPressed(), x, y + s + g, s, "A");
        drawKey(ctx, mc.options.backKey.isPressed(), x + s + g, y + s + g, s, "S");
        drawKey(ctx, mc.options.rightKey.isPressed(), x + (s + g) * 2, y + s + g, s, "D");

        HudPos.recordBounds("keystrokes", x, y, s * 3 + g * 2, s * 2 + g);
    }

    private void drawKey(DrawContext ctx, boolean pressed, int x, int y, int size, String label) {
        ctx.fill(x, y, x + size, y + size, pressed ? BG_ON : BG);
        int tx = x + (size - MinecraftClient.getInstance().textRenderer.getWidth(label)) / 2;
        int ty = y + (size - MinecraftClient.getInstance().textRenderer.fontHeight) / 2;
        ctx.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.literal(label), tx, ty, FG);
    }
}
