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
 * <p>Security note:
 * We store tokens ONLY so the user doesn't need to re-login every time.
 * This file lives in the user's config directory (config/saltclient/spotify.json).
 */
public final class SpotifyAuthStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record Auth(
        String clientId,
        String accessToken,
        String refreshToken,
        long expiresAtMs
    ) {}

    private SpotifyAuthStore() {}

    public static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("saltclient").resolve("spotify.json");
    }

    public static Auth load() {
        Path path = configPath();
        if (!Files.isRegularFile(path)) return empty();

        try (Reader r = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null) return empty();

            // Back-compat: old format stored only accessToken.
            String clientId = str(root, "clientId");
            String accessToken = str(root, "accessToken");
            String refreshToken = str(root, "refreshToken");
            long expiresAt = longVal(root, "expiresAtMs");
            if (expiresAt == 0L) expiresAt = longVal(root, "expiresAt"); // tolerate alt key

            return new Auth(safe(clientId), safe(accessToken), safe(refreshToken), expiresAt);
        } catch (Exception ignored) {
            return empty();
        }
    }

    public static boolean save(Auth auth) {
        if (auth == null) auth = empty();

        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());

            JsonObject root = new JsonObject();
            if (!auth.clientId().isBlank()) root.addProperty("clientId", auth.clientId().trim());
            if (!auth.accessToken().isBlank()) root.addProperty("accessToken", auth.accessToken().trim());
            if (!auth.refreshToken().isBlank()) root.addProperty("refreshToken", auth.refreshToken().trim());
            if (auth.expiresAtMs() > 0L) root.addProperty("expiresAtMs", auth.expiresAtMs());
            root.addProperty("savedAtMs", System.currentTimeMillis());

            try (Writer w = Files.newBufferedWriter(path)) {
                GSON.toJson(root, w);
            }
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static boolean saveClientId(String clientId) {
        if (clientId == null) clientId = "";
        clientId = clientId.trim();

        Auth old = load();
        Auth next = new Auth(clientId, old.accessToken(), old.refreshToken(), old.expiresAtMs());
        return save(next);
    }

    public static void clearTokensKeepClientId() {
        Auth old = load();
        if (old.clientId().isBlank()) {
            clearAll();
            return;
        }
        save(new Auth(old.clientId(), "", "", 0L));
    }

    public static void clearAll() {
        try {
            Files.deleteIfExists(configPath());
        } catch (Exception ignored) {
        }
    }

    private static Auth empty() {
        return new Auth("", "", "", 0L);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String str(JsonObject o, String key) {
        if (o == null || key == null) return "";
        if (!o.has(key)) return "";
        try {
            String s = o.get(key).getAsString();
            return s == null ? "" : s;
        } catch (Exception ignored) {
            return "";
        }
    }

    private static long longVal(JsonObject o, String key) {
        if (o == null || key == null) return 0L;
        if (!o.has(key)) return 0L;
        try {
            return o.get(key).getAsLong();
        } catch (Exception ignored) {
            return 0L;
        }
    }
}

