package com.saltclient.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleManager;
import com.saltclient.state.SaltState;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Very small JSON config:
 * {
 *   "modules": { "keystrokes": true, "fpscounter": false, ... }
 *   "settings": { ... }
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
            if (mods == null) return;

            for (Module m : modules.all()) {
                JsonElement el = mods.get(m.getId());
                if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
                    m.setEnabledFromConfig(el.getAsBoolean());
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
