package com.saltclient.util;

import net.minecraft.client.MinecraftClient;

public final class ActivityTracker {
    private static long lastActiveMs = System.currentTimeMillis();

    private ActivityTracker() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;

        // Any user input counts as activity.
        if (mc.options.forwardKey.isPressed()
            || mc.options.backKey.isPressed()
            || mc.options.leftKey.isPressed()
            || mc.options.rightKey.isPressed()
            || mc.options.jumpKey.isPressed()
            || mc.options.attackKey.isPressed()
            || mc.options.useKey.isPressed()
            || mc.options.sprintKey.isPressed()
            || mc.options.sneakKey.isPressed()) {
            lastActiveMs = System.currentTimeMillis();
        }
    }

    public static long idleMs() {
        return Math.max(0L, System.currentTimeMillis() - lastActiveMs);
    }

    public static void markActive() {
        lastActiveMs = System.currentTimeMillis();
    }
}

