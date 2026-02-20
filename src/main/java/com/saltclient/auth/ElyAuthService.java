package com.saltclient.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.saltclient.mixin.MinecraftClientAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.session.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Minimal Ely.by authenticator using the public Yggdrasil-compatible API.
 *
 * We keep this very small to avoid extra dependencies:
 *  - POST {username,password,requestUser=true} to /authenticate
 *  - On success, apply the received accessToken + profile to the client Session.
 *
 * Note: Ely.by tokens are verified against Ely session servers. Servers that
 * require Mojang / Microsoft auth may still reject the token, but Ely-compatible
 * servers and offline servers will accept it. This is strictly client-side.
 */
public final class ElyAuthService {
    private ElyAuthService() {}

    public record Result(boolean ok, String message) {}

    public static CompletableFuture<Result> loginAsync(String username, String password, String baseUrl) {
        return CompletableFuture.supplyAsync(() -> login(username, password, baseUrl));
    }

    private static Result login(String username, String password, String baseUrl) {
        try {
            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                return new Result(false, "Username and password required.");
            }

            String trimmedBase = baseUrl == null || baseUrl.isBlank() ? "https://authserver.ely.by" : baseUrl.trim();
            if (trimmedBase.endsWith("/")) trimmedBase = trimmedBase.substring(0, trimmedBase.length() - 1);

            URL url = new URL(trimmedBase + "/authenticate");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JsonObject body = new JsonObject();
            body.addProperty("username", username);
            body.addProperty("password", password);
            body.addProperty("requestUser", true);

            try (OutputStreamWriter w = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
                w.write(body.toString());
            }

            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream(),
                StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);

            if (code != HttpURLConnection.HTTP_OK) {
                return new Result(false, "Ely.by auth failed (" + code + "): " + sb);
            }

            JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
            String accessToken = json.get("accessToken").getAsString();
            JsonObject profile = json.getAsJsonObject("selectedProfile");
            if (profile == null) return new Result(false, "No profile returned.");

            String name = profile.get("name").getAsString();
            String id = profile.get("id").getAsString();
            // Ely returns UUID without dashes.
            String dashed = id.length() == 32
                ? id.replaceFirst(
                    "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                    "$1-$2-$3-$4-$5")
                : id;

            UUID uuid = UUID.fromString(dashed);
            Session session = new Session(name, uuid, accessToken, Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.execute(() -> ((MinecraftClientAccessor) mc).salt$setSession(session));

            return new Result(true, "Logged in as " + name + " (Ely.by)");
        } catch (Exception e) {
            return new Result(false, "Auth error: " + e.getMessage());
        }
    }
}
