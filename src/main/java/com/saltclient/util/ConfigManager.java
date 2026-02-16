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
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * JSON config manager.
 *
 * Supports:
 * - Default runtime config (saltclient.json)
 * - Named configs under config/saltclient/configs/*.json
 * - Loading any external JSON file path
 */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path defaultPath;
    private final Path namedConfigsDir;

    public ConfigManager(String modId) {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        this.defaultPath = configDir.resolve(modId + ".json");
        this.namedConfigsDir = configDir.resolve(modId).resolve("configs");
    }

    public Path getNamedConfigsDir() {
        return namedConfigsDir;
    }

    public void load(ModuleManager modules) {
        readConfig(modules, defaultPath);
    }

    public void save(ModuleManager modules) {
        writeConfig(modules, defaultPath);
    }

    public boolean saveNamed(ModuleManager modules, String rawName) {
        Path target = resolveNamedPath(rawName);
        if (target == null) return false;
        return writeConfig(modules, target);
    }

    public boolean loadNamed(ModuleManager modules, String rawName) {
        Path source = resolveNamedPath(rawName);
        if (source == null) return false;
        if (!readConfig(modules, source)) return false;

        // Keep currently active state mirrored into the default runtime config.
        save(modules);
        return true;
    }

    public boolean loadExternal(ModuleManager modules, String rawPath) {
        Path source = resolveExternalPath(rawPath);
        if (source == null) return false;
        if (!readConfig(modules, source)) return false;

        // Keep currently active state mirrored into the default runtime config.
        save(modules);
        return true;
    }

    public List<String> listNamedConfigs() {
        List<String> out = new ArrayList<>();
        if (!Files.isDirectory(namedConfigsDir)) return out;

        try (Stream<Path> files = Files.list(namedConfigsDir)) {
            files.filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.toLowerCase().endsWith(".json"))
                .map(ConfigManager::stripJsonSuffix)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .forEach(out::add);
        } catch (Exception ignored) {
            // Best effort list.
        }

        return out;
    }

    private boolean readConfig(ModuleManager modules, Path source) {
        if (source == null || !Files.exists(source) || !Files.isRegularFile(source)) return false;

        try (Reader r = Files.newBufferedReader(source)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return false;
            applyRoot(modules, root);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean writeConfig(ModuleManager modules, Path target) {
        if (target == null) return false;

        try {
            Files.createDirectories(target.getParent());
            JsonObject root = buildRoot(modules);
            try (Writer w = Files.newBufferedWriter(target)) {
                GSON.toJson(root, w);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static JsonObject buildRoot(ModuleManager modules) {
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

        return root;
    }

    private static void applyRoot(ModuleManager modules, JsonObject root) {
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
    }

    private Path resolveNamedPath(String rawName) {
        String stem = sanitizeName(rawName);
        if (stem.isEmpty()) return null;

        Path root = namedConfigsDir.normalize();
        Path target = root.resolve(stem + ".json").normalize();
        if (!target.startsWith(root)) return null;
        return target;
    }

    private Path resolveExternalPath(String rawPath) {
        if (rawPath == null) return null;

        String value = rawPath.trim();
        if (value.isEmpty()) return null;

        try {
            Path path = Path.of(value);
            if (!path.isAbsolute()) {
                path = namedConfigsDir.resolve(value);
            }

            if (!path.getFileName().toString().toLowerCase().endsWith(".json")) {
                path = path.resolveSibling(path.getFileName() + ".json");
            }

            path = path.normalize();
            if (!Files.exists(path) || !Files.isRegularFile(path)) return null;
            return path;
        } catch (InvalidPathException ignored) {
            return null;
        }
    }

    private static String sanitizeName(String rawName) {
        if (rawName == null) return "";

        String name = rawName.trim();
        if (name.isEmpty()) return "";

        if (name.toLowerCase().endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }

        name = name.replace(' ', '_');
        name = name.replaceAll("[^a-zA-Z0-9._-]", "_");
        while (name.contains("..")) {
            name = name.replace("..", ".");
        }

        return name;
    }

    private static String stripJsonSuffix(String value) {
        if (value.toLowerCase().endsWith(".json")) {
            return value.substring(0, value.length() - 5);
        }
        return value;
    }

    private static int getInt(JsonObject o, String k, int def) {
        JsonElement el = o.get(k);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
            try {
                return el.getAsInt();
            } catch (Exception ignored) {
            }
        }
        return def;
    }

    private static boolean getBool(JsonObject o, String k, boolean def) {
        JsonElement el = o.get(k);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
            try {
                return el.getAsBoolean();
            } catch (Exception ignored) {
            }
        }
        return def;
    }
}
