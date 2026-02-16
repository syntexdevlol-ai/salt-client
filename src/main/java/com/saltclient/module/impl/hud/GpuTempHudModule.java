package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudRenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class GpuTempHudModule extends Module {
    private Path gpuTempPath;
    private long lastDetectMs;
    private long lastReadMs;
    private String cached = "GPU: N/A";

    public GpuTempHudModule() {
        super("gputemphud", "GPUTempHUD", "Shows GPU temperature when available.", ModuleCategory.HUD, true);
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if ((gpuTempPath == null) && (now - lastDetectMs >= 5_000L)) {
            lastDetectMs = now;
            gpuTempPath = detectGpuTempPath();
        }

        if (now - lastReadMs >= 1500L) {
            lastReadMs = now;
            cached = readGpuTemp();
        }

        int y = HudLayout.nextBottomRight(14);
        int x = mc.getWindow().getScaledWidth() - (mc.textRenderer.getWidth(cached) + 8) - 10;
        HudRenderUtil.textBoxHud(ctx, mc.textRenderer, "gputemphud", cached, x, y, 0xFF8BE0FF, 0xAA0E121A);
    }

    private static Path detectGpuTempPath() {
        Path thermalRoot = Paths.get("/sys/class/thermal");
        try {
            if (!Files.isDirectory(thermalRoot)) return null;
            try (var zones = Files.list(thermalRoot)) {
                return zones
                    .filter(p -> p.getFileName().toString().startsWith("thermal_zone"))
                    .filter(p -> {
                        try {
                            Path type = p.resolve("type");
                            if (!Files.exists(type)) return false;
                            String t = Files.readString(type).toLowerCase();
                            return t.contains("gpu") || t.contains("kgsl");
                        } catch (Exception ignored) {
                            return false;
                        }
                    })
                    .map(p -> p.resolve("temp"))
                    .filter(Files::exists)
                    .findFirst()
                    .orElse(null);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private String readGpuTemp() {
        if (gpuTempPath == null) return "GPU: N/A";
        try {
            String raw = Files.readString(gpuTempPath).trim();
            if (raw.isEmpty()) return "GPU: N/A";
            int v = Integer.parseInt(raw);
            double c = v > 1000 ? (v / 1000.0) : v;
            if (c > 0.0 && c < 200.0) {
                return String.format("GPU: %.1fC", c);
            }
        } catch (Exception ignored) {
        }
        return "GPU: N/A";
    }
}
