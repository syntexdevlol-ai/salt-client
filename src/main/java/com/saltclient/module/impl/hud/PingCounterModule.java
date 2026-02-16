package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

public final class PingCounterModule extends Module {
    public PingCounterModule() {
        super("pingcounter", "PingCounter", "Show current ping.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        String text = HudCache.get("pingcounter:text", () -> {
            if (mc.getNetworkHandler() == null || mc.player == null) return null;
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry == null) return null;
            return entry.getLatency() + "ms Ping";
        });
        if (text == null) return;

        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(text) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "pingcounter", text, x, y, 0xFF8BE0FF, 0xAA0E121A);
    }
}
