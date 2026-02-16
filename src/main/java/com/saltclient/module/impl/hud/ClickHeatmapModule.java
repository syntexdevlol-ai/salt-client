package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import com.saltclient.util.InputTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class ClickHeatmapModule extends Module {
    public ClickHeatmapModule() {
        super("clickheatmap", "ClickHeatmap", "Heatmap for left/right click intensity.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int left = InputTracker.CLICKS.getLeftCps();
        int right = InputTracker.CLICKS.getRightCps();

        int w = 80;
        int h = 30;
        int defaultX = 10;
        int defaultY = HudLayout.nextTopLeft(h + 6);
        HudPos.Pos pos = HudPos.resolve("clickheatmap", defaultX, defaultY);
        int x = pos.x;
        int y = pos.y;

        drawBar(ctx, x, y, w, 14, left, "L");
        drawBar(ctx, x, y + 16, w, 14, right, "R");

        HudPos.recordBounds("clickheatmap", x, y, w, h);
    }

    private void drawBar(DrawContext ctx, int x, int y, int w, int h, int cps, String label) {
        int max = 20;
        float t = Math.min(1.0f, cps / (float) max);
        int bg = 0xAA0E121A;
        int fg = blend(0xFF2B3A55, 0xFFFF5C5C, t);

        ctx.fill(x, y, x + w, y + h, bg);
        int fillW = Math.max(2, (int) ((w - 2) * t));
        ctx.fill(x + 1, y + 1, x + 1 + fillW, y + h - 1, fg);
        ctx.drawBorder(x, y, w, h, 0xFF1D2940);

        MinecraftClient mc = MinecraftClient.getInstance();
        String txt = label + " " + cps;
        int tx = x + 4;
        int ty = y + (h - mc.textRenderer.fontHeight) / 2;
        ctx.drawTextWithShadow(mc.textRenderer, Text.literal(txt), tx, ty, 0xFFE6EBFA);
    }

    private static int blend(int a, int b, float t) {
        int ar = (a >> 16) & 0xFF;
        int ag = (a >> 8) & 0xFF;
        int ab = a & 0xFF;

        int br = (b >> 16) & 0xFF;
        int bg = (b >> 8) & 0xFF;
        int bb = b & 0xFF;

        int rr = (int) (ar + (br - ar) * t);
        int rg = (int) (ag + (bg - ag) * t);
        int rb = (int) (ab + (bb - ab) * t);

        return 0xFF000000 | (rr << 16) | (rg << 8) | rb;
    }
}
