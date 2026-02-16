package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class MatchTimerModule extends Module {
    public MatchTimerModule() {
        super("matchtimer", "MatchTimer", "Shows elapsed time since world/server join.", ModuleCategory.HUD, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc == null || mc.world == null) return;
        if (SaltState.matchStartMs <= 0L) {
            SaltState.matchStartMs = System.currentTimeMillis();
        }
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || SaltState.matchStartMs <= 0L) return;

        long elapsedMs = System.currentTimeMillis() - SaltState.matchStartMs;
        String text = "Match: " + format(elapsedMs);
        int y = HudLayout.nextBottomLeft(14);
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "matchtimer", text, 10, y, 0xFFE6EBFA, 0xAA0E121A);
    }

    private static String format(long ms) {
        long s = ms / 1000L;
        long h = s / 3600L;
        long m = (s % 3600L) / 60L;
        long ss = s % 60L;
        return String.format("%02d:%02d:%02d", h, m, ss);
    }
}
