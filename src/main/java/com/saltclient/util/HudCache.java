package com.saltclient.util;

import com.saltclient.SaltClient;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Small cache for HUD strings to reduce allocations / work when HUDCache is enabled.
 */
public final class HudCache {
    private static final class Entry {
        long lastUpdateMs;
        String value;
    }

    private static final Map<String, Entry> CACHE = new HashMap<>();

    private HudCache() {}

    public static String get(String key, Supplier<String> compute) {
        if (!SaltClient.MODULES.isEnabled("hudcache")) {
            return compute.get();
        }

        long now = System.currentTimeMillis();
        Entry e = CACHE.computeIfAbsent(key, k -> new Entry());

        // Update at most ~5 times per second.
        if (e.value == null || (now - e.lastUpdateMs) > 200L) {
            e.value = compute.get();
            e.lastUpdateMs = now;
        }

        return e.value;
    }

    public static void clear() {
        CACHE.clear();
    }
}

