package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;

public final class SessionTimeModule extends Module {
    private ClientWorld lastWorld;
    private long startMs;

    public SessionTimeModule() {
        super("sessiontime", "SessionTime", "Show time since joining the current world/server.", ModuleCategory.HUD, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null) {
            lastWorld = null;
            startMs = 0;
            return;
        }

        if (mc.world != lastWorld) {
            lastWorld = mc.world;
            startMs = System.currentTimeMillis();
        }
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || startMs <= 0) return;

        String text = HudCache.get("sessiontime:text", () -> {
            long elapsed = System.currentTimeMillis() - startMs;
            return "Session: " + format(elapsed);
        });

        int y = HudLayout.nextBottomLeft(14);
        int x = 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "sessiontime", text, x, y, 0xFFE6ECFF, 0xAA0E121A);
    }

    private static String format(long ms) {
        long s = ms / 1000L;
        long h = s / 3600L;
        long m = (s % 3600L) / 60L;
        long ss = s % 60L;
        return String.format("%02d:%02d:%02d", h, m, ss);
    }
}
