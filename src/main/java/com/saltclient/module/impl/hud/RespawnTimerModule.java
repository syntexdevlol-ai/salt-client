package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class RespawnTimerModule extends Module {
    public RespawnTimerModule() {
        super("respawntimer", "RespawnTimer", "Shows time since your last death.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || SaltState.lastDeathMs <= 0L) return;

        long elapsed = System.currentTimeMillis() - SaltState.lastDeathMs;
        boolean show = mc.player.isDead() || elapsed <= 10_000L;
        if (!show) return;

        String text = String.format("Respawn: %.1fs", elapsed / 1000.0);
        int w = mc.textRenderer.getWidth(text) + 8;
        int x = (mc.getWindow().getScaledWidth() - w) / 2;
        int y = (mc.getWindow().getScaledHeight() / 2) + 32;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "respawntimer", text, x, y, 0xFFFFD66E, 0xAA0E121A);
    }
}
