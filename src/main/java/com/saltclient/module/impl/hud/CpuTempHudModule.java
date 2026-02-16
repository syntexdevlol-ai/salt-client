package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.nio.file.Files;
import java.nio.file.Path;

public final class CpuTempHudModule extends Module {
    private static final Path[] CANDIDATES = new Path[] {
        Path.of("/sys/class/thermal/thermal_zone0/temp"),
        Path.of("/sys/class/thermal/thermal_zone1/temp"),
        Path.of("/sys/class/thermal/thermal_zone2/temp")
    };

    private long lastReadMs;
    private String cached = "CPU: N/A";

    public CpuTempHudModule() {
        super("cputemphud", "CPUTempHUD", "Shows CPU temperature when available.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastReadMs >= 1500L) {
            lastReadMs = now;
            cached = readTemp("CPU");
        }

        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(cached) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "cputemphud", cached, x, y, 0xFFFFD66E, 0xAA0E121A);
    }

    private static String readTemp(String label) {
        for (Path p : CANDIDATES) {
            try {
                if (!Files.exists(p)) continue;
                String raw = Files.readString(p).trim();
                if (raw.isEmpty()) continue;
                int v = Integer.parseInt(raw);
                double c = v > 1000 ? (v / 1000.0) : v;
                if (c > 0.0 && c < 200.0) {
                    return String.format("%s: %.1fC", label, c);
                }
            } catch (Exception ignored) {
            }
        }
        return label + ": N/A";
    }
}
