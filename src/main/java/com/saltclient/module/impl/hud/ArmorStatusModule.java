package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public final class ArmorStatusModule extends Module {
    public ArmorStatusModule() {
        super("armorstatus", "ArmorStatus", "Show equipped armor + durability.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Above hotbar, left side
        int defaultX = 10;
        int defaultY = sh - 78;
        HudPos.Pos pos = HudPos.resolve("armorstatus", defaultX, defaultY);
        int x = pos.x;
        int y = pos.y;

        // PlayerInventory#getArmorStack order is boots->helmet (0..3). We'll render helmet first.
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().getArmorStack(i);
            if (!stack.isEmpty()) {
                ctx.drawItem(stack, x, y);

                if (stack.isDamageable()) {
                    int remaining = stack.getMaxDamage() - stack.getDamage();
                    String txt = String.valueOf(remaining);
                    ctx.drawTextWithShadow(mc.textRenderer, txt, x + 18 - mc.textRenderer.getWidth(txt), y + 18, 0xFFE6ECFF);
                }
            }
            x += 20;
        }

        HudPos.recordBounds("armorstatus", pos.x, pos.y, 20 * 4, 20);
    }
}
