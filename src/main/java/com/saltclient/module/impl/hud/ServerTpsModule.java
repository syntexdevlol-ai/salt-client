package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import com.saltclient.util.ServerTpsTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ServerTpsModule extends Module {
    public ServerTpsModule() {
        super("servertps", "ServerTPS", "Show estimated server TPS.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = HudCache.get("servertps:text", () -> String.format("TPS: %.1f", ServerTpsTracker.getTps()));

        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(text) - 14;
        HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFF7BC96F, 0xAA0E121A);
    }
}
