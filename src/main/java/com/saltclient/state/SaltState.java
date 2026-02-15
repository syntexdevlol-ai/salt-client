package com.saltclient.state;

import net.minecraft.util.math.MathHelper;

/**
 * Shared runtime state used by mixins / render hooks.
 *
 * Prefer storing only what we cannot easily derive from modules each tick.
 */
public final class SaltState {
    private SaltState() {}

    // --- Zoom ---
    public static boolean zooming;
    public static int zoomFov = 30; // smaller = more zoom

    // --- FreeLook ---
    public static boolean freeLookActive;
    public static float freeLookYaw;
    public static float freeLookPitch;

    // --- Crosshair ---
    public static int crosshairSize = 6;
    public static int crosshairGap = 3;
    public static int crosshairThickness = 2;
    public static boolean crosshairDot = false;
    // ARGB
    public static int crosshairColor = 0xFFE6ECFF;

    // --- Hit marker ---
    public static long lastHitMs;
    public static long lastKillMs;

    // --- Screenshot helper ---
    public static long lastScreenshotMs;
    public static String lastScreenshotName;

    public static void clampCrosshair() {
        crosshairSize = MathHelper.clamp(crosshairSize, 2, 20);
        crosshairGap = MathHelper.clamp(crosshairGap, 0, 15);
        crosshairThickness = MathHelper.clamp(crosshairThickness, 1, 6);
    }

    public static void clampZoom() {
        zoomFov = MathHelper.clamp(zoomFov, 10, 70);
    }
}

