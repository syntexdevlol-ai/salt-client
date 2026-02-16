package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class TabListPlayerCountModule extends Module {
    public TabListPlayerCountModule() {
        super("tablistplayercount", "TabListPlayerCount", "Shows player count from tab list.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        int count = mc.getNetworkHandler().getPlayerList().size();
        String text = "Players: " + count;

        int y = HudLayout.nextTopRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(text) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "tablistplayercount", text, x, y, 0xFFE6EBFA, 0xAA0E121A);
    }
}
