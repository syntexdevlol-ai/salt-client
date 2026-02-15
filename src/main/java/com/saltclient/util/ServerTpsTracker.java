package com.saltclient.util;

/**
 * Estimates server TPS by observing time update packets.
 *
 * Works best on servers that regularly send time updates.
 */
public final class ServerTpsTracker {
    private static long lastTimeUpdateMs;
    private static long lastWorldTime;
    private static double tps = 20.0;

    private ServerTpsTracker() {}

    public static void onWorldTimeUpdate(long worldTime) {
        long now = System.currentTimeMillis();

        if (lastTimeUpdateMs != 0L) {
            long dtMs = now - lastTimeUpdateMs;
            long dTicks = worldTime - lastWorldTime;

            if (dtMs > 0L && dTicks > 0L && dTicks < 10_000L) {
                double inst = (dTicks * 1000.0) / dtMs;
                // Smooth a bit.
                tps = (tps * 0.8) + (inst * 0.2);
                if (tps > 20.0) tps = 20.0;
                if (tps < 0.0) tps = 0.0;
            }
        }

        lastTimeUpdateMs = now;
        lastWorldTime = worldTime;
    }

    public static double getTps() {
        return tps;
    }
}

