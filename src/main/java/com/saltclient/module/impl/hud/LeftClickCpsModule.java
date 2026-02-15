package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import com.saltclient.util.InputTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class LeftClickCpsModule extends Module {
    public LeftClickCpsModule() {
        super("leftclickcps", "LeftClickCPS", "Show left click CPS only.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = HudCache.get("leftclickcps:text", () -> "L-CPS: " + InputTracker.CLICKS.getLeftCps());

        int y = HudLayout.nextTopRight(14);
        int x = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(text) - 14;
        HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
    }
}
