package com.saltclient.tweaks;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

public final class WorldTweaks {
    private WorldTweaks() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null) return;
        ClientWorld w = mc.world;
        if (w == null) return;

        if (SaltClient.MODULES.isEnabled("timechanger")) {
            // Force noon client-side.
            w.setTimeOfDay(6000L);
        }

        if (SaltClient.MODULES.isEnabled("weatherdisabler")) {
            w.getLevelProperties().setRaining(false);
            w.setLightningTicksLeft(0);
        } else if (SaltClient.MODULES.isEnabled("weatherchanger")) {
            w.getLevelProperties().setRaining(true);
        }
    }
}

