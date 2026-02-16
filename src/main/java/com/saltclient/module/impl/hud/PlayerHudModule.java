package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudCache;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerHudModule extends Module {
    public PlayerHudModule() {
        super("playerhud", "PlayerHUD", "Show basic player stats.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity p = mc.player;
        if (p == null) return;

        String hp = HudCache.get("playerhud:hp", () -> String.format("HP: %.1f/%.1f", p.getHealth(), p.getMaxHealth()));
        String food = HudCache.get("playerhud:food", () -> "Food: " + p.getHungerManager().getFoodLevel());
        String armor = HudCache.get("playerhud:armor", () -> "Armor: " + p.getArmor());

        int maxW = Math.max(mc.textRenderer.getWidth(hp), Math.max(mc.textRenderer.getWidth(food), mc.textRenderer.getWidth(armor)));
        int boxW = maxW + 8; // padX*2
        int boxH = mc.textRenderer.fontHeight + 4; // padY*2

        int defaultX = 10;
        int defaultY = HudLayout.nextTopLeft(14 * 3);
        HudPos.Pos pos = HudPos.resolve("playerhud", defaultX, defaultY);

        int x = pos.x;
        int y = pos.y;
        HudRenderUtil.textBoxTL(ctx, mc.textRenderer, hp, x, y, 0xFFFF9AD5, 0xAA0E121A);
        HudRenderUtil.textBoxTL(ctx, mc.textRenderer, food, x, y + 14, 0xFFFFD66E, 0xAA0E121A);
        HudRenderUtil.textBoxTL(ctx, mc.textRenderer, armor, x, y + 28, 0xFF8BE0FF, 0xAA0E121A);

        HudPos.recordBounds("playerhud", x, y, boxW, 28 + boxH);
    }
}
