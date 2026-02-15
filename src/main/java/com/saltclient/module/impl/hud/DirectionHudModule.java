package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;

public final class DirectionHudModule extends Module {
    public DirectionHudModule() {
        super("directionhud", "DirectionHUD", "Show facing direction.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = HudCache.get("directionhud:text", () -> {
            Direction dir = mc.player.getHorizontalFacing();
            return "Dir: " + dir.asString().toUpperCase();
        });

        int y = HudLayout.nextBottomLeft(14);
        int x = 10;
        HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
    }
}
