package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class StreakCounterModule extends Module {
    public StreakCounterModule() {
        super("streakcounter", "StreakCounter", "Shows current and best kill streak.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        String text = "Streak: " + SaltState.streakCount + "  Best: " + SaltState.bestStreak;
        int y = HudLayout.nextBottomLeft(14);
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "streakcounter", text, 10, y, 0xFFFFD66E, 0xAA0E121A);
    }
}
