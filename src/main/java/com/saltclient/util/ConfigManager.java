package com.saltclient.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleManager;
import com.saltclient.setting.Setting;
import com.saltclient.state.SaltState;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Very small JSON config:
 * {
 *   "modules": { "keystrokes": true, "fpscounter": false, ... }
 *   "settings": { ... }
 *   "hud": { "fpscounter": {"x": 10, "y": 10}, ... }
 *   "moduleSettings": { "zoom": {"key": 67, "keyMode": "HOLD", "zoomFov": 30}, ... }
 * }
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path path;

    public ConfigManager(String modId) {
        this.path = FabricLoader.getInstance().getConfigDir().resolve(modId + ".json");
    }

    public void load(ModuleManager modules) {
        if (!Files.exists(path)) return;

        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return;

            JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : null;
            if (mods != null) {
                for (Module m : modules.all()) {
                    JsonElement el = mods.get(m.getId());
                    if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                        m.setEnabledFromConfig(el.getAsBoolean());
                    }
                }
            }

            JsonObject settings = root.has("settings") ? root.getAsJsonObject("settings") : null;
            if (settings != null) {
                SaltState.crosshairSize = getInt(settings, "crosshairSize", SaltState.crosshairSize);
                SaltState.crosshairGap = getInt(settings, "crosshairGap", SaltState.crosshairGap);
                SaltState.crosshairThickness = getInt(settings, "crosshairThickness", SaltState.crosshairThickness);
                SaltState.crosshairDot = getBool(settings, "crosshairDot", SaltState.crosshairDot);
                SaltState.crosshairColor = getInt(settings, "crosshairColor", SaltState.crosshairColor);
                SaltState.zoomFov = getInt(settings, "zoomFov", SaltState.zoomFov);

                SaltState.clampCrosshair();
                SaltState.clampZoom();
            }

            JsonObject hud = root.has("hud") ? root.getAsJsonObject("hud") : null;
            if (hud != null) {
                for (Map.Entry<String, JsonElement> e : hud.entrySet()) {
                    if (e.getValue() == null || !e.getValue().isJsonObject()) continue;
                    JsonObject p = e.getValue().getAsJsonObject();
                    int x = getInt(p, "x", Integer.MIN_VALUE);
                    int y = getInt(p, "y", Integer.MIN_VALUE);
                    if (x != Integer.MIN_VALUE && y != Integer.MIN_VALUE) {
                        HudPos.set(e.getKey(), x, y);
                    }
                }
            }

            // Per-module settings (keybinds, modes, etc).
            JsonObject moduleSettings = root.has("moduleSettings") ? root.getAsJsonObject("moduleSettings") : null;
            if (moduleSettings != null) {
                for (Module m : modules.all()) {
                    JsonObject ms = moduleSettings.has(m.getId()) ? moduleSettings.getAsJsonObject(m.getId()) : null;
                    if (ms == null) continue;
                    for (Setting<?> s : m.getSettings()) {
                        JsonElement el = ms.get(s.getId());
                        if (el != null) s.fromJson(el);
                    }
                }
            }
        } catch (Exception ignored) {
            // If config is broken, we just start with defaults.
        }
    }

    public void save(ModuleManager modules) {
        try {
            Files.createDirectories(path.getParent());

            JsonObject root = new JsonObject();
            JsonObject mods = new JsonObject();
            for (Module m : modules.all()) {
                mods.addProperty(m.getId(), m.isEnabled());
            }
            root.add("modules", mods);

            JsonObject settings = new JsonObject();
            settings.addProperty("crosshairSize", SaltState.crosshairSize);
            settings.addProperty("crosshairGap", SaltState.crosshairGap);
            settings.addProperty("crosshairThickness", SaltState.crosshairThickness);
            settings.addProperty("crosshairDot", SaltState.crosshairDot);
            settings.addProperty("crosshairColor", SaltState.crosshairColor);
            settings.addProperty("zoomFov", SaltState.zoomFov);
            root.add("settings", settings);

            JsonObject hud = new JsonObject();
            for (Map.Entry<String, HudPos.Pos> e : HudPos.allPositions().entrySet()) {
                JsonObject p = new JsonObject();
                p.addProperty("x", e.getValue().x);
                p.addProperty("y", e.getValue().y);
                hud.add(e.getKey(), p);
            }
            root.add("hud", hud);

            JsonObject moduleSettings = new JsonObject();
            for (Module m : modules.all()) {
                if (m.getSettings().isEmpty()) continue;
                JsonObject ms = new JsonObject();
                for (Setting<?> s : m.getSettings()) {
                    ms.add(s.getId(), s.toJson());
                }
                moduleSettings.add(m.getId(), ms);
            }
            root.add("moduleSettings", moduleSettings);

            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(root, w);
            }
        } catch (Exception ignored) {
            // Best-effort: a failed save should never crash the game.
        }
    }

    private static int getInt(JsonObject o, String k, int def) {
        JsonElement el = o.get(k);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
            try {
                return el.getAsInt();
            } catch (Exception ignored) {}
        }
        return def;
    }

    private static boolean getBool(JsonObject o, String k, boolean def) {
        JsonElement el = o.get(k);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
            try {
                return el.getAsBoolean();
            } catch (Exception ignored) {}
        }
        return def;
    }
}
