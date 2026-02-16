package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.EntityHitResult;

public final class ComboCounterModule extends Module {
    private int combo;
    private long lastHitMs;
    private int lastEntityId = -1;
    private boolean prevAttack;

    public ComboCounterModule() {
        super("combocounter", "ComboCounter", "Shows a simple combo counter (client-side estimate).", ModuleCategory.HUD, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;

        boolean attack = mc.options.attackKey.isPressed();
        if (attack && !prevAttack) {
            long now = System.currentTimeMillis();

            Entity e = null;
            if (mc.crosshairTarget instanceof EntityHitResult ehr) {
                e = ehr.getEntity();
            }

            if (e != null) {
                if (e.getId() == lastEntityId && (now - lastHitMs) <= 1500L) combo++;
                else combo = 1;

                lastEntityId = e.getId();
                lastHitMs = now;
            }
        }
        prevAttack = attack;

        if (combo > 0 && (System.currentTimeMillis() - lastHitMs) > 1500L) {
            combo = 0;
            lastEntityId = -1;
        }
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || combo <= 0) return;

        String text = "Combo: " + combo;
        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(text) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "combocounter", text, x, y, 0xFFFF9AD5, 0xAA0E121A);
    }
}
