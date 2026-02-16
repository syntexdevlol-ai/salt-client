package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.Perspective;

public final class ReplayIndicatorModule extends Module {
    public ReplayIndicatorModule() {
        super("replayindicator", "ReplayIndicator", "Shows an indicator for camera modes.", ModuleCategory.MISC, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options == null) return;

        Perspective p = mc.options.getPerspective();
        boolean third = p != null && p != Perspective.FIRST_PERSON;

        if (!third && !SaltState.freeLookActive) return;

        String text = SaltState.freeLookActive ? "FREELOOK" : "3RD PERSON";
        int w = mc.getWindow().getScaledWidth();
        int x = w - (mc.textRenderer.getWidth(text) + 8) - 10;
        int y = 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "replayindicator", text, x, y, 0xFFFF5C5C, 0xAA0E121A);
    }
}
