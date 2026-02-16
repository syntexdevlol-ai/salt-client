package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class KillCounterModule extends Module {
    public KillCounterModule() {
        super("killcounter", "KillCounter", "Shows kills in current match/session.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = "Kills: " + SaltState.killCount;
        int y = HudLayout.nextBottomLeft(14);
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "killcounter", text, 10, y, 0xFF7BC96F, 0xAA0E121A);
    }
}
