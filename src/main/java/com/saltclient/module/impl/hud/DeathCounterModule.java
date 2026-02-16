package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class DeathCounterModule extends Module {
    public DeathCounterModule() {
        super("deathcounter", "DeathCounter", "Shows deaths in current match/session.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = "Deaths: " + SaltState.deathCount;
        int y = HudLayout.nextBottomLeft(14);
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "deathcounter", text, 10, y, 0xFFFF9AD5, 0xAA0E121A);
    }
}
