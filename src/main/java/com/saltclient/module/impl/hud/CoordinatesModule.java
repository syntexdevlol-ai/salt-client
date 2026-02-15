package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class CoordinatesModule extends Module {
    public CoordinatesModule() {
        super("coordinates", "Coordinates", "Show player coordinates.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = HudCache.get(
            "coordinates:text",
            () -> String.format("XYZ: %.1f %.1f %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ())
        );
        int y = HudLayout.nextBottomLeft(14);
        int x = 10;
        HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
    }
}
