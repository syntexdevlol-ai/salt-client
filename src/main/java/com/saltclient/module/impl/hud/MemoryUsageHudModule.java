package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class MemoryUsageHudModule extends Module {
    public MemoryUsageHudModule() {
        super("memoryusagehud", "MemoryUsageHUD", "Shows JVM memory usage.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L);
        long max = rt.maxMemory() / (1024L * 1024L);

        String text = "RAM: " + used + "MB / " + max + "MB";
        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(text) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "memoryusagehud", text, x, y, 0xFF8BE0FF, 0xAA0E121A);
    }
}
