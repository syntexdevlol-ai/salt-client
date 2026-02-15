package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class ReachDisplayModule extends Module {
    public ReachDisplayModule() {
        super("reachdisplay", "ReachDisplay", "Display distance to crosshair target.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.crosshairTarget == null) return;

        Vec3d from = mc.player.getEyePos();
        Vec3d to = null;

        if (mc.crosshairTarget instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            to = e.getPos().add(0, e.getHeight() * 0.5, 0);
        } else if (mc.crosshairTarget instanceof BlockHitResult bhr) {
            BlockPos p = bhr.getBlockPos();
            to = Vec3d.ofCenter(p);
        }

        if (to == null) return;

        double dist = from.distanceTo(to);
        String text = HudCache.get("reachdisplay:text", () -> String.format("Reach: %.2f", dist));

        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(text) - 14;
        HudRenderUtil.textBox(ctx, mc.textRenderer, text, x, y, 0xFFE6ECFF, 0xAA0E121A);
    }
}
