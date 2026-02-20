package com.saltclient.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Tiny Modrinth API helper used by the in-game "Browse" installer.
 *
 * Kept dependency-free (uses Minecraft's built-in Gson).
 */
public final class ModrinthApi {
    private static final String API = "https://api.modrinth.com/v2";

    private ModrinthApi() {}

    public record Project(String id, String slug, String title, String description, int downloads) {}

    public record Asset(String filename, String url) {}

    public static List<Project> search(String query, InstallType type, int limit) throws IOException {
        String q = query == null ? "" : query.trim();
        if (q.isEmpty()) return List.of();

        int realLimit = Math.max(1, Math.min(50, limit));
        String facets = facetsFor(type);

        String endpoint = API
            + "/search?query=" + enc(q)
            + "&limit=" + realLimit
            + "&index=relevance"
            + "&facets=" + enc(facets);

        JsonObject root = requestJsonObject(endpoint);
        if (root == null) return List.of();

        JsonArray hits = root.has("hits") && root.get("hits").isJsonArray() ? root.getAsJsonArray("hits") : null;
        if (hits == null || hits.isEmpty()) return List.of();

        List<Project> out = new ArrayList<>();
        for (JsonElement el : hits) {
            if (!el.isJsonObject()) continue;
            JsonObject hit = el.getAsJsonObject();

            String id = str(hit, "project_id");
            String slug = str(hit, "slug");
            String title = str(hit, "title");
            String desc = str(hit, "description");
            int downloads = intVal(hit, "downloads");

            if (id == null || title == null) continue;
            if (desc == null) desc = "";

            out.add(new Project(id, slug == null ? "" : slug, title, desc, downloads));
        }

        return out;
    }

    /**
     * Resolve a direct download URL for the latest compatible version.
     *
     * <p>For mods, we prefer Fabric loader versions. For packs/worlds, we don't apply loader filtering.
     */
    public static Asset resolveLatestAsset(String projectIdOrSlug, InstallType type, String gameVersion) throws IOException {
        String id = projectIdOrSlug == null ? "" : projectIdOrSlug.trim();
        if (id.isEmpty()) return null;

        StringBuilder endpoint = new StringBuilder(API)
            .append("/project/")
            .append(encPath(id))
            .append("/version");

        boolean hasParam = false;

        if (type == InstallType.MOD) {
            endpoint.append("?loaders=").append(enc("[\"fabric\"]"));
            hasParam = true;
        }

        String gv = gameVersion == null ? "" : gameVersion.trim();
        if (!gv.isEmpty()) {
            endpoint.append(hasParam ? "&" : "?");
            endpoint.append("game_versions=").append(enc("[\"" + gv + "\"]"));
        }

        JsonArray versions = requestJsonArray(endpoint.toString());
        if (versions == null || versions.isEmpty()) return null;

        for (JsonElement versionEl : versions) {
            if (!versionEl.isJsonObject()) continue;
            JsonObject version = versionEl.getAsJsonObject();
            JsonArray files = version.has("files") && version.get("files").isJsonArray() ? version.getAsJsonArray("files") : null;
            if (files == null || files.isEmpty()) continue;

            Asset fallback = null;
            for (JsonElement fileEl : files) {
                if (!fileEl.isJsonObject()) continue;
                JsonObject file = fileEl.getAsJsonObject();

                String filename = str(file, "filename");
                String url = str(file, "url");
                if (filename == null || url == null) continue;
                if (!matchesType(filename, type)) continue;

                boolean primary = bool(file, "primary");
                Asset asset = new Asset(filename, url);
                if (primary) return asset;
                if (fallback == null) fallback = asset;
            }
            if (fallback != null) return fallback;
        }

        return null;
    }

    private static String facetsFor(InstallType type) {
        // Search facets are a JSON array of arrays:
        // - outer list = AND
        // - inner list = OR
        String projectType = switch (type) {
            case MOD -> "mod";
            case TEXTURE_PACK -> "resourcepack";
            case WORLD -> "world";
        };

        // Keep it simple and broad; we filter exact versions later during install.
        return "[[\"project_type:" + projectType + "\"]]";
    }

    private static boolean matchesType(String fileName, InstallType type) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return switch (type) {
            case MOD -> lower.endsWith(".jar");
            case TEXTURE_PACK, WORLD -> lower.endsWith(".zip");
        };
    }

    private static String enc(String value) throws IOException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String encPath(String value) throws IOException {
        // Encode as a single path segment (URLEncoder is OK here; just fix space encoding).
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private static JsonArray requestJsonArray(String url) throws IOException {
        JsonElement element = requestJson(url);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static JsonObject requestJsonObject(String url) throws IOException {
        JsonElement element = requestJson(url);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonElement requestJson(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "saltclient-installer/1.0");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Modrinth API error: HTTP " + code);
        }

        try (InputStream in = conn.getInputStream()) {
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    private static String str(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key)) return null;
        JsonElement e = obj.get(key);
        if (e == null || !e.isJsonPrimitive()) return null;
        try {
            return e.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static int intVal(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key)) return 0;
        JsonElement e = obj.get(key);
        if (e == null || !e.isJsonPrimitive()) return 0;
        try {
            return e.getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static boolean bool(JsonObject obj, String key) {
        if (obj == null || key == null || !obj.has(key)) return false;
        JsonElement e = obj.get(key);
        if (e == null || !e.isJsonPrimitive()) return false;
        try {
            return e.getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }
}
