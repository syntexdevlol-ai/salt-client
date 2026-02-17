package com.saltclient.spotify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Tiny on-disk store for Spotify auth values.
 *
 * <p>We only store an access token here (optional), because Spotify tokens are sensitive.
 * Users can also keep it in-memory by not pressing the "Save" button.
 */
public final class SpotifyAuthStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SpotifyAuthStore() {}

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("saltclient").resolve("spotify.json");
    }

    public static String loadAccessToken() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) return "";

        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return "";
            if (!root.has("accessToken")) return "";
            String token = root.get("accessToken").getAsString();
            return token == null ? "" : token.trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    public static boolean saveAccessToken(String token) {
        if (token == null) token = "";
        token = token.trim();
        if (token.isEmpty()) return false;

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("accessToken", token);
            root.addProperty("savedAt", System.currentTimeMillis());
            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(root, w);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void clear() {
        try {
            Files.deleteIfExists(configPath());
        } catch (Exception ignored) {
        }
    }
}

