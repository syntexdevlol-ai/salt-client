package com.saltclient.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Persistent HUD positions + per-frame bounds for HUD editor hit testing.
 */
public final class HudPos {
    public static final class Pos {
        public int x;
        public int y;

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static final class Bounds {
        public int x;
        public int y;
        public int w;
        public int h;

        public Bounds(int x, int y, int w, int h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    private static final Map<String, Pos> POS = new HashMap<>();
    private static final Map<String, Bounds> BOUNDS = new HashMap<>();

    private HudPos() {}

    public static void beginFrame() {
        BOUNDS.clear();
    }

    public static Map<String, Pos> allPositions() {
        return POS;
    }

    public static Map<String, Bounds> allBounds() {
        return Collections.unmodifiableMap(BOUNDS);
    }

    public static Pos resolve(String id, int defaultX, int defaultY) {
        Pos p = POS.get(id);
        return p != null ? p : new Pos(defaultX, defaultY);
    }

    public static void set(String id, int x, int y) {
        POS.put(id, new Pos(x, y));
    }

    public static void clear(String id) {
        POS.remove(id);
    }

    public static void resetAll() {
        POS.clear();
    }

    public static void recordBounds(String id, int x, int y, int w, int h) {
        if (id == null) return;
        BOUNDS.put(id, new Bounds(x, y, w, h));
    }
}

