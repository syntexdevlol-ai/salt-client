package com.saltclient.module.impl.combat;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HitColorModule extends Module {
    public HitColorModule() {
        super("hitcolor", "HitColor", "Shows a colored hit marker on attack (client-side).", ModuleCategory.COMBAT, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (SaltState.lastHitMs == 0L || (now - SaltState.lastHitMs) > 150L) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        int c = 0xFFFF5C5C;
        int s = 6;
        int t = 2;

        // Simple "X" marker
        ctx.fill(cx - s, cy - s, cx - s + t, cy - s + t, c);
        ctx.fill(cx + s - t, cy - s, cx + s, cy - s + t, c);
        ctx.fill(cx - s, cy + s - t, cx - s + t, cy + s, c);
        ctx.fill(cx + s - t, cy + s - t, cx + s, cy + s, c);
    }
}

