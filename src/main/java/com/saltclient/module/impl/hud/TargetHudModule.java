package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;

public final class TargetHudModule extends Module {
    private static final int BG = 0xCC0E121A;
    private static final int BORDER = 0xFF2B3A55;
    private static final int TEXT = 0xFFE6ECFF;
    private static final int HEALTH = 0xFF7BC96F;

    public TargetHudModule() {
        super("targethud", "TargetHUD", "Show a small target HUD when aiming at an entity.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;

        Entity e = ehr.getEntity();
        if (!(e instanceof LivingEntity le)) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        int w = 150;
        int h = 28;
        int x = (sw - w) / 2;
        int y = sh - 80; // above the hotbar

        ctx.fill(x, y, x + w, y + h, BG);
        ctx.drawBorder(x, y, w, h, BORDER);

        String name = le.getDisplayName().getString();
        String hp = String.format("%.1f/%.1f", le.getHealth(), le.getMaxHealth());

        ctx.drawTextWithShadow(mc.textRenderer, name, x + 8, y + 6, TEXT);
        int hpW = mc.textRenderer.getWidth(hp);
        ctx.drawTextWithShadow(mc.textRenderer, hp, x + w - hpW - 8, y + 6, HEALTH);
    }
}
