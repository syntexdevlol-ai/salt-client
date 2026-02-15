package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class ScreenshotHelperModule extends Module {
    public ScreenshotHelperModule() {
        super("screenshothelper", "ScreenshotHelper", "Shows a small indicator when a screenshot is taken.", ModuleCategory.MISC, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (SaltState.lastScreenshotMs == 0L || (now - SaltState.lastScreenshotMs) > 3000L) return;

        String name = (SaltState.lastScreenshotName == null) ? "screenshot" : SaltState.lastScreenshotName;
        String text = "Screenshot: " + name;

        int x = 10;
        int y = 10;
        HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
    }
}

