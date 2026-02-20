package com.saltclient.util;

import net.minecraft.client.gui.DrawContext;

/**
 * Small UI drawing helpers (rounded rectangles).
 *
 * <p>We avoid custom shaders here to keep it simple and Pojav-friendly.
 * Rounded corners are approximated by filling short scanlines.
 */
public final class UiDraw {
    private UiDraw() {}

    public static void fillRounded(DrawContext ctx, int x1, int y1, int x2, int y2, int radius, int color) {
        if (ctx == null) return;
        int w = x2 - x1;
        int h = y2 - y1;
        if (w <= 0 || h <= 0) return;

        int r = Math.max(0, radius);
        int maxR = Math.min(w / 2, h / 2);
        if (r > maxR) r = maxR;
        if (r <= 0) {
            ctx.fill(x1, y1, x2, y2, color);
            return;
        }

        // Center + side bands.
        ctx.fill(x1 + r, y1, x2 - r, y2, color);
        ctx.fill(x1, y1 + r, x2, y2 - r, color);

        int rr = r * r;
        for (int dy = 0; dy < r; dy++) {
            // dy is measured from the top of the corner box.
            // The circle center is at (r, r), so vertical distance from center is (r - dy).
            int dyFromCenter = r - dy;
            int dx = (int) Math.floor(Math.sqrt(Math.max(0, rr - (dyFromCenter * dyFromCenter))));

            // Top-left
            ctx.fill(x1 + r - dx, y1 + dy, x1 + r, y1 + dy + 1, color);
            // Top-right
            ctx.fill(x2 - r, y1 + dy, x2 - r + dx, y1 + dy + 1, color);
            // Bottom-left
            ctx.fill(x1 + r - dx, y2 - dy - 1, x1 + r, y2 - dy, color);
            // Bottom-right
            ctx.fill(x2 - r, y2 - dy - 1, x2 - r + dx, y2 - dy, color);
        }
    }

    public static void panelRounded(DrawContext ctx, int x, int y, int w, int h, int radius, int fill, int border) {
        if (w <= 0 || h <= 0) return;
        // Border
        fillRounded(ctx, x, y, x + w, y + h, radius, border);
        // Inner fill
        fillRounded(ctx, x + 1, y + 1, x + w - 1, y + h - 1, Math.max(0, radius - 1), fill);
    }
}
