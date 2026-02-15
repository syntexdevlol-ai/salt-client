package com.saltclient.module.impl.crosshair;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class CustomCrosshairModule extends Module {
    public CustomCrosshairModule() {
        super("customcrosshair", "CustomCrosshair", "Render a configurable custom crosshair.", ModuleCategory.CROSSHAIR, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        int size = SaltState.crosshairSize;
        int gap = SaltState.crosshairGap;
        int t = SaltState.crosshairThickness;
        int c = SaltState.crosshairColor;

        // Horizontal
        ctx.fill(cx - gap - size, cy - t / 2, cx - gap, cy + (t + 1) / 2, c);
        ctx.fill(cx + gap, cy - t / 2, cx + gap + size, cy + (t + 1) / 2, c);
        // Vertical
        ctx.fill(cx - t / 2, cy - gap - size, cx + (t + 1) / 2, cy - gap, c);
        ctx.fill(cx - t / 2, cy + gap, cx + (t + 1) / 2, cy + gap + size, c);

        if (SaltState.crosshairDot) {
            ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
        }
    }
}

