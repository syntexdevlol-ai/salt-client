package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

public final class PotionHudModule extends Module {
    public PotionHudModule() {
        super("potionhud", "PotionHUD", "Show active potion effects.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
            String name = inst.getEffectType().value().getName().getString();
            int amp = inst.getAmplifier();
            if (amp > 0) name += " " + (amp + 1);

            String dur = formatDuration(inst.getDuration());
            String text = name + " (" + dur + ")";

            int y = HudLayout.nextTopRight(14);
            int x = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(text) - 14;
            HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
        }
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%d:%02d", m, s);
    }
}
