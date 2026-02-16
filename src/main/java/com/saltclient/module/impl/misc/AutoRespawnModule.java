package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;

public final class AutoRespawnModule extends Module {
    public AutoRespawnModule() {
        super("autorespawn", "AutoRespawn", "Automatically respawn after death.", ModuleCategory.MISC, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof DeathScreen)) return;

        mc.player.requestRespawn();
        mc.setScreen(null);
    }
}
