package com.saltclient.util;

import net.minecraft.client.MinecraftClient;

/**
 * Tiny "layout manager" for HUD modules.
 *
 * We keep this dead simple: modules can ask for the next Y position in a corner,
 * which prevents overlapping without having to implement a full draggable HUD editor.
 */
public final class HudLayout {
    private static int topLeftY;
    private static int topRightY;
    private static int bottomLeftY;
    private static int bottomRightY;

    private HudLayout() {}

    public static void beginFrame(MinecraftClient mc) {
        int sh = mc.getWindow().getScaledHeight();
        topLeftY = 10;
        topRightY = 10;
        bottomLeftY = sh - 10;
        bottomRightY = sh - 10;
    }

    public static int nextTopLeft(int height) {
        int y = topLeftY;
        topLeftY += height;
        return y;
    }

    public static int nextTopRight(int height) {
        int y = topRightY;
        topRightY += height;
        return y;
    }

    public static int nextBottomLeft(int height) {
        bottomLeftY -= height;
        return bottomLeftY;
    }

    public static int nextBottomRight(int height) {
        bottomRightY -= height;
        return bottomRightY;
    }
}

