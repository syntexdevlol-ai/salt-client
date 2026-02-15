package com.saltclient.module.impl.movement;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;

public final class InventoryWalkModule extends Module {
    public InventoryWalkModule() {
        super("inventorywalk", "InventoryWalk", "Move while a GUI is open (except chat).", ModuleCategory.MOVEMENT, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        Screen s = mc.currentScreen;
        if (s == null) return;
        if (s instanceof ChatScreen) return;

        // Vanilla stops updating input while screens are open. We keep it ticking.
        mc.player.input.tick(mc.player.shouldSlowDown(), 1.0f);
    }
}

