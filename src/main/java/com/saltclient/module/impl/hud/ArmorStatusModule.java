package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public final class ArmorStatusModule extends Module {
    private final BoolSetting showDurability;
    private final BoolSetting vertical;
    private final BoolSetting compact;
    private final IntSetting spacing;

    public ArmorStatusModule() {
        super("armorstatus", "ArmorStatus", "Show equipped armor + durability.", ModuleCategory.HUD, true);
        this.showDurability = addSetting(new BoolSetting("showDurability", "Show Durability", "Render remaining durability text.", true));
        this.vertical = addSetting(new BoolSetting("vertical", "Vertical", "Render armor in a vertical column.", false));
        this.compact = addSetting(new BoolSetting("compact", "Compact", "Hide empty armor slots.", false));
        this.spacing = addSetting(new IntSetting("spacing", "Spacing", "Distance between armor slots.", 20, 16, 28, 1));
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
        int baseX = pos.x;
        int baseY = pos.y;
        int step = spacing.getValue();

        boolean drawVertical = vertical.getValue();
        boolean drawCompact = compact.getValue();
        boolean drawDurability = showDurability.getValue();

        // PlayerInventory#getArmorStack order is boots->helmet (0..3). We'll render helmet first.
        int rendered = 0;
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().getArmorStack(i);
            if (drawCompact && stack.isEmpty()) continue;

            int x = baseX + (drawVertical ? 0 : rendered * step);
            int y = baseY + (drawVertical ? rendered * step : 0);

            if (!stack.isEmpty()) {
                ctx.drawItem(stack, x, y);

                if (drawDurability && stack.isDamageable()) {
                    int remaining = stack.getMaxDamage() - stack.getDamage();
                    String txt = String.valueOf(remaining);
                    ctx.drawTextWithShadow(mc.textRenderer, txt, x + 18 - mc.textRenderer.getWidth(txt), y + 18, 0xFFE6EBFA);
                }
            }
            rendered++;
        }

        int count = Math.max(1, rendered);
        int width = drawVertical ? 20 : (20 + ((count - 1) * step));
        int height = drawVertical ? (20 + ((count - 1) * step)) : 20;
        HudPos.recordBounds("armorstatus", pos.x, pos.y, width, height);
    }
}
