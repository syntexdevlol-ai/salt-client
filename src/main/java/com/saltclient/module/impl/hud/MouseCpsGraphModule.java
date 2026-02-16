package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import com.saltclient.util.InputTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class MouseCpsGraphModule extends Module {
    private static final int HISTORY = 60;

    private final int[] left = new int[HISTORY];
    private final int[] right = new int[HISTORY];
    private int index;
    private long lastSampleMs;

    public MouseCpsGraphModule() {
        super("mousecpsgraph", "MouseCPSGraph", "Graph of left/right CPS over time.", ModuleCategory.HUD, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        long now = System.currentTimeMillis();
        if (now - lastSampleMs < 100L) return;
        lastSampleMs = now;

        index = (index + 1) % HISTORY;
        left[index] = InputTracker.CLICKS.getLeftCps();
        right[index] = InputTracker.CLICKS.getRightCps();
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int w = 124;
        int h = 42;
        int defaultX = mc.getWindow().getScaledWidth() - w - 10;
        int defaultY = HudLayout.nextTopRight(h + 6);
        HudPos.Pos pos = HudPos.resolve("mousecpsgraph", defaultX, defaultY);
        int x = pos.x;
        int y = pos.y;

        ctx.fill(x, y, x + w, y + h, 0xAA0E121A);
        ctx.drawBorder(x, y, w, h, 0xFF2B3A55);

        int graphX = x + 2;
        int graphY = y + 2;
        int graphW = w - 4;
        int graphH = h - 4;
        int maxCps = 20;

        for (int i = 0; i < graphW; i++) {
            int sampleIdx = Math.floorMod(index - (graphW - 1 - i), HISTORY);
            int l = Math.min(maxCps, left[sampleIdx]);
            int r = Math.min(maxCps, right[sampleIdx]);

            int lh = (l * graphH) / maxCps;
            int rh = (r * graphH) / maxCps;

            int px = graphX + i;
            ctx.fill(px, graphY + graphH - lh, px + 1, graphY + graphH, 0xFF7BC96F);
            ctx.fill(px, graphY + graphH - rh, px + 1, graphY + graphH, 0xFF8BE0FF);
        }

        HudPos.recordBounds("mousecpsgraph", x, y, w, h);
    }
}
