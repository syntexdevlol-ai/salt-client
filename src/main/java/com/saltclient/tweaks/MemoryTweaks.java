package com.saltclient.tweaks;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class MemoryTweaks {
    private static long lastGcMs;

    private MemoryTweaks() {}

    public static void tick(MinecraftClient mc) {
        if (!SaltClient.MODULES.isEnabled("gcoptimizer")) return;

        long now = System.currentTimeMillis();
        if (now - lastGcMs < 120_000L) return;

        if (memoryPressure() >= 0.85) {
            lastGcMs = now;
            System.gc();
        }
    }

    public static void runCleaner(MinecraftClient mc) {
        System.gc();
        lastGcMs = System.currentTimeMillis();
        if (mc != null && mc.player != null) {
            mc.player.sendMessage(Text.literal("Salt: requested GC"), true);
        }
    }

    private static double memoryPressure() {
        Runtime rt = Runtime.getRuntime();
        long max = rt.maxMemory();
        long used = rt.totalMemory() - rt.freeMemory();
        if (max <= 0) return 0;
        return (double) used / (double) max;
    }
}

