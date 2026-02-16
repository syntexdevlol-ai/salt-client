package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.effect.StatusEffectInstance;

import java.util.ArrayList;
import java.util.List;

public final class PotionHudModule extends Module {
    public PotionHudModule() {
        super("potionhud", "PotionHUD", "Show active potion effects.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        List<String> lines = new ArrayList<>();
        for (StatusEffectInstance inst : mc.player.getStatusEffects()) {
            String name = inst.getEffectType().value().getName().getString();
            int amp = inst.getAmplifier();
            if (amp > 0) name += " " + (amp + 1);

            String dur = formatDuration(inst.getDuration());
            lines.add(name + " (" + dur + ")");
        }

        if (lines.isEmpty()) return;

        int maxW = 0;
        for (String s : lines) maxW = Math.max(maxW, mc.textRenderer.getWidth(s));

        int boxH = mc.textRenderer.fontHeight + 4;

        int defaultY = HudLayout.nextTopRight(14 * lines.size());
        int defaultX = mc.getWindow().getScaledWidth() - (maxW + 8) - 10;
        HudPos.Pos pos = HudPos.resolve("potionhud", defaultX, defaultY);

        int y = pos.y;
        for (String text : lines) {
            int x = pos.x + (maxW - mc.textRenderer.getWidth(text));
            HudRenderUtil.textBoxTL(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
            y += 14;
        }

        int w = maxW + 8;
        int h = (14 * (lines.size() - 1)) + boxH;
        HudPos.recordBounds("potionhud", pos.x, pos.y, w, h);
    }

    private static String formatDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int m = totalSeconds / 60;
        int s = totalSeconds % 60;
        return String.format("%d:%02d", m, s);
    }
}
