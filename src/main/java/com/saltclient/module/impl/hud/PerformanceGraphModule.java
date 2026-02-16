package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class PerformanceGraphModule extends Module {
    private static final int HISTORY = 90;
    private static final int SAMPLE_MS = 120;

    private final int[] fps = new int[HISTORY];
    private int index;
    private long lastSampleMs;

    public PerformanceGraphModule() {
        super("performancegraph", "PerformanceGraph", "Shows a compact FPS history graph.", ModuleCategory.HUD, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        long now = System.currentTimeMillis();
        if (now - lastSampleMs < SAMPLE_MS) return;
        lastSampleMs = now;

        index = (index + 1) % HISTORY;
        fps[index] = Math.max(0, mc.getCurrentFps());
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int w = 96;
        int h = 36;
        int defaultX = mc.getWindow().getScaledWidth() - w - 10;
        int defaultY = 10;
        HudPos.Pos pos = HudPos.resolve("performancegraph", defaultX, defaultY);
        int x = pos.x;
        int y = pos.y;

        ctx.fill(x, y, x + w, y + h, 0xAA0E121A);
        ctx.drawBorder(x, y, w, h, 0xFF2B3A55);

        int graphX = x + 3;
        int graphY = y + 3;
        int graphW = w - 6;
        int graphH = h - 6;

        int maxFps = 180;
        for (int i = 0; i < graphW; i++) {
            int sampleIdx = (index - (graphW - 1 - i) + HISTORY) % HISTORY;
            int v = Math.min(maxFps, fps[sampleIdx]);
            int barH = (v * graphH) / maxFps;
            int px = graphX + i;
            int color = v >= 120 ? 0xFF7BC96F : (v >= 60 ? 0xFFFFD66E : 0xFFFF5C5C);
            ctx.fill(px, graphY + graphH - barH, px + 1, graphY + graphH, color);
        }

        HudPos.recordBounds("performancegraph", x, y, w, h);
    }
}
