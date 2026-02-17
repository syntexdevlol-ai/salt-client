package com.saltclient.spotify;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spotify OAuth (PKCE) helper.
 *
 * Why we need this:
 * - Spotify does not allow username/password login from third-party apps.
 * - The correct approach is OAuth, which results in an access token (and refresh token).
 *
 * Important limitation:
 * - Spotify does NOT provide raw audio streams. This is remote control only.
 */
public final class SpotifyOAuth {
    private static final String AUTH_URL = "https://accounts.spotify.com/authorize";
    private static final String TOKEN_URL = "https://accounts.spotify.com/api/token";

    // Keep this stable: the redirect URI must be whitelisted in the user's Spotify app settings.
    public static final int PORT = 23225;
    public static final String REDIRECT_URI = "http://127.0.0.1:" + PORT + "/callback";

    private static final String SCOPES = String.join(
        " ",
        "user-read-currently-playing",
        "user-read-playback-state",
        "user-modify-playback-state"
    );

    private static final SecureRandom RAND = new SecureRandom();
    private static final AtomicBoolean LOGIN_IN_PROGRESS = new AtomicBoolean(false);

    private SpotifyOAuth() {}

    public interface Listener {
        void info(String msg);
        void success(String msg);
        void error(String msg);
        void onLoggedIn();
    }

    public static boolean isLoginInProgress() {
        return LOGIN_IN_PROGRESS.get();
    }

    /**
     * Starts the Spotify login flow:
     * - opens the user's browser for authorization
     * - receives the callback on localhost
     * - exchanges the code for access+refresh tokens
     * - saves tokens to config
     */
    public static void startLogin(String clientId, Listener listener) {
        if (listener == null) return;

        clientId = clientId == null ? "" : clientId.trim();
        if (clientId.isEmpty()) {
            listener.error("Missing Client ID");
            return;
        }
        final String finalClientId = clientId;

        if (!LOGIN_IN_PROGRESS.compareAndSet(false, true)) {
            listener.error("Login already in progress");
            return;
        }

        final String state = randomUrlSafe(18);
        final String verifier = randomUrlSafe(64);
        final String challenge;
        try {
            challenge = base64Url(sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
        } catch (Exception e) {
            LOGIN_IN_PROGRESS.set(false);
            listener.error("Failed to start OAuth: " + safeMsg(e));
            return;
        }

        final HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        } catch (BindException e) {
            LOGIN_IN_PROGRESS.set(false);
            listener.error("OAuth port " + PORT + " is busy. Close other apps and try again.");
            return;
        } catch (IOException e) {
            LOGIN_IN_PROGRESS.set(false);
            listener.error("Failed to start OAuth callback server: " + safeMsg(e));
            return;
        }

        server.createContext("/callback", ex -> {
            try {
                handleCallback(ex, finalClientId, state, verifier, listener, server);
            } catch (Exception e) {
                writeHtml(ex, 500, "<h3>Salt Client</h3><p>Login failed.</p>");
                onMain(() -> listener.error("OAuth callback error: " + safeMsg(e)));
                stopServer(server);
                LOGIN_IN_PROGRESS.set(false);
            }
        });

        server.start();

        // Timeout safety: don't keep the server open forever.
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000L);
            } catch (InterruptedException ignored) {
            }
            if (LOGIN_IN_PROGRESS.get()) {
                stopServer(server);
                LOGIN_IN_PROGRESS.set(false);
                onMain(() -> listener.error("Login timed out"));
            }
        }, "salt-spotify-oauth-timeout").start();

        String url = buildAuthorizeUrl(clientId, state, challenge);
        onMain(() -> {
            // Always copy to clipboard as fallback (Pojav/Android sometimes can't open URLs).
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.keyboard.setClipboard(url);

            try {
                Util.getOperatingSystem().open(URI.create(url));
                listener.info("Browser opened. If not, URL copied to clipboard.");
            } catch (Exception e) {
                listener.info("URL copied to clipboard. Open it in your browser to login.");
            }

            listener.info("Waiting for Spotify login... (redirect: " + REDIRECT_URI + ")");
        });
    }

    /**
     * Loads token data from disk and refreshes it if needed.
     *
     * Call this from a background thread (it may perform network requests).
     */
    public static String getValidAccessTokenBlocking() throws IOException {
        SpotifyAuthStore.Auth auth = SpotifyAuthStore.load();
        if (auth.accessToken().isBlank()) return "";

        long now = System.currentTimeMillis();
        boolean hasExpiry = auth.expiresAtMs() > 0L;
        boolean expiredOrSoon = hasExpiry && now > (auth.expiresAtMs() - 30_000L);

        if (!expiredOrSoon) return auth.accessToken();

        if (auth.refreshToken().isBlank() || auth.clientId().isBlank()) {
            // Can't refresh; fall back to current access token (may 401).
            return auth.accessToken();
        }

        SpotifyAuthStore.Auth refreshed = refresh(auth);
        SpotifyAuthStore.save(refreshed);
        return refreshed.accessToken();
    }

    public static SpotifyAuthStore.Auth refresh(SpotifyAuthStore.Auth auth) throws IOException {
        if (auth == null) auth = SpotifyAuthStore.load();
        if (auth.refreshToken().isBlank()) throw new IOException("Missing refresh token");
        if (auth.clientId().isBlank()) throw new IOException("Missing client id");

        String body = form(Map.of(
            "client_id", auth.clientId().trim(),
            "grant_type", "refresh_token",
            "refresh_token", auth.refreshToken().trim()
        ));

        JsonObject json = postFormJson(TOKEN_URL, body);
        String accessToken = str(json, "access_token");
        String refreshToken = str(json, "refresh_token"); // may be missing
        int expiresIn = intVal(json, "expires_in");

        if (accessToken.isBlank()) throw new IOException("Refresh failed: missing access_token");
        if (refreshToken.isBlank()) refreshToken = auth.refreshToken();

        long expiresAt = expiresIn <= 0 ? 0L : (System.currentTimeMillis() + (expiresIn * 1000L));
        return new SpotifyAuthStore.Auth(auth.clientId(), accessToken, refreshToken, expiresAt);
    }

    private static void handleCallback(
        HttpExchange ex,
        String clientId,
        String expectedState,
        String verifier,
        Listener listener,
        HttpServer server
    ) throws IOException {
        URI uri = ex.getRequestURI();
        Map<String, String> q = parseQuery(uri == null ? "" : uri.getRawQuery());

        String error = q.getOrDefault("error", "");
        if (!error.isBlank()) {
            writeHtml(ex, 200, "<h3>Salt Client</h3><p>Spotify login error: " + escapeHtml(error) + "</p>");
            onMain(() -> listener.error("Spotify login error: " + error));
            stopServer(server);
            LOGIN_IN_PROGRESS.set(false);
            return;
        }

        String state = q.getOrDefault("state", "");
        String code = q.getOrDefault("code", "");
        if (code.isBlank() || state.isBlank()) {
            writeHtml(ex, 400, "<h3>Salt Client</h3><p>Missing code/state.</p>");
            onMain(() -> listener.error("OAuth callback missing code/state"));
            stopServer(server);
            LOGIN_IN_PROGRESS.set(false);
            return;
        }
        if (!expectedState.equals(state)) {
            writeHtml(ex, 400, "<h3>Salt Client</h3><p>State mismatch.</p>");
            onMain(() -> listener.error("OAuth state mismatch"));
            stopServer(server);
            LOGIN_IN_PROGRESS.set(false);
            return;
        }

        writeHtml(ex, 200, "<h3>Salt Client</h3><p>Login complete. You can close this tab.</p>");
        stopServer(server);

        onMain(() -> listener.info("Exchanging code for tokens..."));

        final String authCode = code;
        new Thread(() -> {
            try {
                SpotifyAuthStore.Auth tokens = exchangeCode(clientId, authCode, verifier);
                SpotifyAuthStore.save(tokens);

                onMain(() -> {
                    listener.success("Spotify login successful");
                    listener.onLoggedIn();
                });
            } catch (Exception e) {
                onMain(() -> listener.error("Token exchange failed: " + safeMsg(e)));
            } finally {
                LOGIN_IN_PROGRESS.set(false);
            }
        }, "salt-spotify-oauth-exchange").start();
    }

    private static SpotifyAuthStore.Auth exchangeCode(String clientId, String code, String verifier) throws IOException {
        String body = form(Map.of(
            "client_id", clientId.trim(),
            "grant_type", "authorization_code",
            "code", code.trim(),
            "redirect_uri", REDIRECT_URI,
            "code_verifier", verifier
        ));

        JsonObject json = postFormJson(TOKEN_URL, body);

        String accessToken = str(json, "access_token");
        String refreshToken = str(json, "refresh_token");
        int expiresIn = intVal(json, "expires_in");

        if (accessToken.isBlank()) throw new IOException("Login failed: missing access_token");
        if (refreshToken.isBlank()) throw new IOException("Login failed: missing refresh_token");

        long expiresAt = expiresIn <= 0 ? 0L : (System.currentTimeMillis() + (expiresIn * 1000L));
        return new SpotifyAuthStore.Auth(clientId, accessToken, refreshToken, expiresAt);
    }

    private static String buildAuthorizeUrl(String clientId, String state, String challenge) {
        // Keep query building simple and strict.
        return AUTH_URL +
            "?client_id=" + enc(clientId) +
            "&response_type=code" +
            "&redirect_uri=" + enc(REDIRECT_URI) +
            "&state=" + enc(state) +
            "&scope=" + enc(SCOPES) +
            "&code_challenge_method=S256" +
            "&code_challenge=" + enc(challenge);
    }

    private static JsonObject postFormJson(String url, String body) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(20000);
        conn.setInstanceFollowRedirects(true);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "saltclient-spotify/1.1");

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(bytes);
        }

        int code = conn.getResponseCode();
        InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
        if (in == null) throw new IOException("HTTP " + code);

        try (InputStream bodyStream = in) {
            JsonElement el = JsonParser.parseReader(new java.io.InputStreamReader(bodyStream, StandardCharsets.UTF_8));
            if (code < 200 || code >= 300) {
                String msg = el != null && el.isJsonObject() ? str(el.getAsJsonObject(), "error_description") : "";
                if (msg.isBlank()) msg = "HTTP " + code;
                throw new IOException(msg);
            }
            return el != null && el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
        }
    }

    private static void writeHtml(HttpExchange ex, int code, String html) {
        if (ex == null) return;
        try {
            byte[] bytes = ("<!doctype html><html><head><meta charset=\"utf-8\"></head><body style=\"font-family:sans-serif;\">" + html + "</body></html>")
                .getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            ex.sendResponseHeaders(code, bytes.length);
            try (OutputStream out = ex.getResponseBody()) {
                out.write(bytes);
            }
        } catch (Exception ignored) {
        } finally {
            try {
                ex.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void stopServer(HttpServer server) {
        try {
            if (server != null) server.stop(0);
        } catch (Exception ignored) {
        }
    }

    private static void onMain(Runnable r) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) mc.execute(r);
    }

    private static String form(Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : values.entrySet()) {
            if (!first) sb.append('&');
            first = false;
            sb.append(enc(e.getKey())).append('=').append(enc(e.getValue()));
        }
        return sb.toString();
    }

    private static String enc(String s) {
        if (s == null) s = "";
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> out = new HashMap<>();
        if (rawQuery == null || rawQuery.isEmpty()) return out;

        String[] parts = rawQuery.split("&");
        for (String p : parts) {
            if (p.isEmpty()) continue;
            int eq = p.indexOf('=');
            String k = eq >= 0 ? p.substring(0, eq) : p;
            String v = eq >= 0 ? p.substring(eq + 1) : "";
            out.put(urlDecode(k), urlDecode(v));
        }
        return out;
    }

    private static String urlDecode(String s) {
        if (s == null) return "";
        try {
            return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return s;
        }
    }

    private static byte[] sha256(byte[] input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(input);
    }

    private static String randomUrlSafe(int bytes) {
        byte[] b = new byte[Math.max(8, bytes)];
        RAND.nextBytes(b);
        return base64Url(b);
    }

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String str(JsonObject o, String key) {
        if (o == null || key == null) return "";
        JsonElement el = o.get(key);
        if (el == null || !el.isJsonPrimitive()) return "";
        try {
            String s = el.getAsString();
            return s == null ? "" : s;
        } catch (Exception ignored) {
            return "";
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

    private static String safeMsg(Throwable t) {
        if (t == null) return "Unknown";
        String msg = t.getMessage();
        if (msg == null || msg.isBlank()) msg = t.getClass().getSimpleName();
        if (msg.length() > 160) msg = msg.substring(0, 160) + "...";
        return msg;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
