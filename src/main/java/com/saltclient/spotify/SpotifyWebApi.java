package com.saltclient.spotify;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal Spotify Web API helper.
 *
 * Important limitation:
 * Spotify's Web API does NOT provide raw audio streams.
 * This integration is only for Spotify Connect remote control + searching metadata.
 */
public final class SpotifyWebApi {
    private static final String API = "https://api.spotify.com/v1";

    private SpotifyWebApi() {}

    public record NowPlaying(
        boolean isPlaying,
        String title,
        String artists,
        String uri,
        int progressMs,
        int durationMs
    ) {}

    public record Track(
        String title,
        String artists,
        String uri,
        int durationMs
    ) {}

    public static NowPlaying getNowPlaying(String accessToken) throws IOException {
        HttpURLConnection conn = open("GET", API + "/me/player/currently-playing", accessToken);
        int code = conn.getResponseCode();

        if (code == 204) return null; // Nothing playing
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }

        JsonObject root = readJsonObject(conn);
        if (root == null) return null;

        boolean isPlaying = bool(root, "is_playing");
        int progress = intVal(root, "progress_ms");

        JsonObject item = obj(root, "item");
        if (item == null) return null;

        String title = str(item, "name");
        String uri = str(item, "uri");
        int duration = intVal(item, "duration_ms");

        String artists = "";
        JsonArray arr = arr(item, "artists");
        if (arr != null) {
            List<String> names = new ArrayList<>();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) continue;
                String name = str(el.getAsJsonObject(), "name");
                if (name != null && !name.isBlank()) names.add(name);
            }
            artists = String.join(", ", names);
        }

        return new NowPlaying(isPlaying, safe(title), safe(artists), safe(uri), progress, duration);
    }

    public static void pause(String accessToken) throws IOException {
        HttpURLConnection conn = open("PUT", API + "/me/player/pause", accessToken);
        int code = conn.getResponseCode();
        if (code != 204 && (code < 200 || code >= 300)) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }
    }

    public static void resume(String accessToken) throws IOException {
        HttpURLConnection conn = open("PUT", API + "/me/player/play", accessToken);
        conn.setDoOutput(true);
        // Spotify allows an empty body; still send JSON content-type for consistency.
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        try (OutputStream out = conn.getOutputStream()) {
            out.write("{}".getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 204 && (code < 200 || code >= 300)) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }
    }

    public static void next(String accessToken) throws IOException {
        HttpURLConnection conn = open("POST", API + "/me/player/next", accessToken);
        int code = conn.getResponseCode();
        if (code != 204 && (code < 200 || code >= 300)) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }
    }

    public static void previous(String accessToken) throws IOException {
        HttpURLConnection conn = open("POST", API + "/me/player/previous", accessToken);
        int code = conn.getResponseCode();
        if (code != 204 && (code < 200 || code >= 300)) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }
    }

    public static void playTrack(String accessToken, String trackUri) throws IOException {
        if (trackUri == null || trackUri.isBlank()) throw new IOException("Missing track URI");

        HttpURLConnection conn = open("PUT", API + "/me/player/play", accessToken);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        String json = "{\"uris\":[\"" + escapeJson(trackUri.trim()) + "\"]}";
        try (OutputStream out = conn.getOutputStream()) {
            out.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code != 204 && (code < 200 || code >= 300)) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }
    }

    public static List<Track> searchTracks(String accessToken, String query, int limit) throws IOException {
        if (query == null) query = "";
        query = query.trim();
        if (query.isEmpty()) return List.of();

        limit = Math.max(1, Math.min(50, limit));
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = API + "/search?type=track&limit=" + limit + "&q=" + q;

        HttpURLConnection conn = open("GET", url, accessToken);
        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + ": " + readError(conn));
        }

        JsonObject root = readJsonObject(conn);
        JsonObject tracks = obj(root, "tracks");
        JsonArray items = tracks == null ? null : arr(tracks, "items");
        if (items == null) return List.of();

        List<Track> out = new ArrayList<>();
        for (JsonElement el : items) {
            if (!el.isJsonObject()) continue;
            JsonObject t = el.getAsJsonObject();

            String title = str(t, "name");
            String uri = str(t, "uri");
            int duration = intVal(t, "duration_ms");

            String artists = "";
            JsonArray a = arr(t, "artists");
            if (a != null) {
                List<String> names = new ArrayList<>();
                for (JsonElement ae : a) {
                    if (!ae.isJsonObject()) continue;
                    String n = str(ae.getAsJsonObject(), "name");
                    if (n != null && !n.isBlank()) names.add(n);
                }
                artists = String.join(", ", names);
            }

            if (uri == null || uri.isBlank()) continue;
            out.add(new Track(safe(title), safe(artists), uri, duration));
        }

        return out;
    }

    private static HttpURLConnection open(String method, String url, String accessToken) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setInstanceFollowRedirects(true);

        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "saltclient-spotify/1.0");

        if (accessToken == null) accessToken = "";
        accessToken = accessToken.trim();
        if (!accessToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        }

        return conn;
    }

    private static JsonObject readJsonObject(HttpURLConnection conn) throws IOException {
        try (InputStream in = conn.getInputStream()) {
            JsonElement el = JsonParser.parseReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
        }
    }

    private static String readError(HttpURLConnection conn) {
        try (InputStream in = conn.getErrorStream()) {
            if (in == null) return "";
            byte[] bytes = in.readAllBytes();
            String msg = new String(bytes, StandardCharsets.UTF_8).trim();
            if (msg.length() > 300) msg = msg.substring(0, 300) + "...";
            return msg;
        } catch (Exception ignored) {
            return "";
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static JsonObject obj(JsonObject o, String key) {
        if (o == null || key == null) return null;
        JsonElement el = o.get(key);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    private static JsonArray arr(JsonObject o, String key) {
        if (o == null || key == null) return null;
        JsonElement el = o.get(key);
        return el != null && el.isJsonArray() ? el.getAsJsonArray() : null;
    }

    private static String str(JsonObject o, String key) {
        if (o == null || key == null) return null;
        JsonElement el = o.get(key);
        if (el == null || !el.isJsonPrimitive()) return null;
        try {
            return el.getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean bool(JsonObject o, String key) {
        if (o == null || key == null) return false;
        JsonElement el = o.get(key);
        if (el == null || !el.isJsonPrimitive()) return false;
        try {
            return el.getAsBoolean();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static int intVal(JsonObject o, String key) {
        if (o == null || key == null) return 0;
        JsonElement el = o.get(key);
        if (el == null || !el.isJsonPrimitive()) return 0;
        try {
            return el.getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }
}

