package com.saltclient.util;

import net.minecraft.client.MinecraftClient;

/**
 * Tracks simple input state without consuming keybind press events.
 *
 * We use rising-edge detection on keybind pressed states. This is less perfect
 * than hooking raw mouse events, but it is stable and doesn't interfere with gameplay.
 */
public final class InputTracker {
    public static final ClickTracker CLICKS = new ClickTracker();

    private static boolean prevAttack;
    private static boolean prevUse;

    private InputTracker() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;

        boolean attack = mc.options.attackKey.isPressed();
        if (attack && !prevAttack) CLICKS.recordLeft();
        prevAttack = attack;

        boolean use = mc.options.useKey.isPressed();
        if (use && !prevUse) CLICKS.recordRight();
        prevUse = use;
    }
}

