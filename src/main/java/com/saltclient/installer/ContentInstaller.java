package com.saltclient.installer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class ContentInstaller {
    private static final Pattern GITHUB_REPO = Pattern.compile("https?://github\\.com/([^/]+)/([^/?#]+).*");

    private ContentInstaller() {}

    public static void installAsync(String input, InstallType type, Consumer<String> progress, BiConsumer<Boolean, String> done) {
        new Thread(() -> {
            try {
                String value = input == null ? "" : input.trim();
                if (value.isEmpty()) {
                    done.accept(false, "Please enter a URL or project slug");
                    return;
                }

                progress.accept("Resolving source...");
                String downloadUrl = resolveDownloadUrl(value, type);
                if (downloadUrl == null || downloadUrl.isBlank()) {
                    done.accept(false, "Could not resolve download URL");
                    return;
                }

                progress.accept("Downloading...");
                DownloadResult download = downloadFile(downloadUrl, type.preferredExt);

                progress.accept("Installing...");
                Path installed = installDownloaded(download.path(), download.fileName(), type);
                done.accept(true, "Installed to " + installed);
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null || message.isBlank()) {
                    message = e.getClass().getSimpleName();
                }
                done.accept(false, message);
            }
        }, "salt-installer").start();
    }

    public static Path destination(InstallType type) {
        Path game = FabricLoader.getInstance().getGameDir();
        return switch (type) {
            case MOD -> game.resolve("mods");
            case TEXTURE_PACK -> game.resolve("resourcepacks");
            case WORLD -> game.resolve("saves");
        };
    }

    private static String resolveDownloadUrl(String input, InstallType type) throws IOException {
        String lower = input.toLowerCase(Locale.ROOT);

        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            if (lower.contains("modrinth.com/")) {
                String slug = parseModrinthSlug(input);
                if (slug != null) {
                    String url = resolveModrinthAsset(slug, type);
                    if (url != null) return url;
                }
            }

            if (lower.contains("github.com/") && !lower.contains("/releases/download/")) {
                String direct = toRawGithubBlob(input);
                if (!direct.equals(input)) return direct;

                String url = resolveGithubLatestAsset(input, type);
                if (url != null) return url;
            }

            return input;
        }

        // Plain slug: assume Modrinth project slug.
        return resolveModrinthAsset(input, type);
    }

    private static String parseModrinthSlug(String value) {
        try {
            URI uri = URI.create(value);
            String path = uri.getPath();
            if (path == null) return null;

            String[] parts = path.split("/");
            for (int i = 0; i < parts.length - 1; i++) {
                String p = parts[i];
                if ("mod".equals(p) || "resourcepack".equals(p) || "plugin".equals(p) || "modpack".equals(p) || "world".equals(p)) {
                    String slug = parts[i + 1].trim();
                    if (!slug.isEmpty()) return slug;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String resolveModrinthAsset(String projectId, InstallType type) throws IOException {
        String encoded = URLEncoder.encode(projectId, StandardCharsets.UTF_8);
        String endpoint = "https://api.modrinth.com/v2/project/" + encoded + "/version";
        JsonArray versions = requestJsonArray(endpoint);
        if (versions == null || versions.isEmpty()) return null;

        for (JsonElement versionEl : versions) {
            if (!versionEl.isJsonObject()) continue;
            JsonObject version = versionEl.getAsJsonObject();
            JsonArray files = version.has("files") && version.get("files").isJsonArray() ? version.getAsJsonArray("files") : null;
            if (files == null) continue;

            String fallbackUrl = null;
            for (JsonElement fileEl : files) {
                if (!fileEl.isJsonObject()) continue;
                JsonObject file = fileEl.getAsJsonObject();

                String filename = str(file, "filename");
                String url = str(file, "url");
                if (url == null || filename == null) continue;
                if (!matchesType(filename, type)) continue;

                boolean primary = bool(file, "primary");
                if (primary) return url;
                if (fallbackUrl == null) fallbackUrl = url;
            }
            if (fallbackUrl != null) return fallbackUrl;
        }

        return null;
    }

    private static String resolveGithubLatestAsset(String repoUrl, InstallType type) throws IOException {
        Matcher matcher = GITHUB_REPO.matcher(repoUrl);
        if (!matcher.matches()) return null;

        String owner = matcher.group(1);
        String repo = matcher.group(2);
        if (repo.endsWith(".git")) repo = repo.substring(0, repo.length() - 4);

        String endpoint = "https://api.github.com/repos/" + owner + "/" + repo + "/releases/latest";
        JsonObject release = requestJsonObject(endpoint);
        if (release == null) return null;

        JsonArray assets = release.has("assets") && release.get("assets").isJsonArray() ? release.getAsJsonArray("assets") : null;
        if (assets == null || assets.isEmpty()) return null;

        String fallback = null;
        for (JsonElement assetEl : assets) {
            if (!assetEl.isJsonObject()) continue;
            JsonObject asset = assetEl.getAsJsonObject();

            String name = str(asset, "name");
            String url = str(asset, "browser_download_url");
            if (name == null || url == null) continue;
            if (!matchesType(name, type)) continue;

            if (fallback == null) fallback = url;
            if (name.toLowerCase(Locale.ROOT).endsWith(type.preferredExt)) {
                return url;
            }
        }

        return fallback;
    }

    private static String toRawGithubBlob(String url) {
        if (!url.contains("github.com/") || !url.contains("/blob/")) return url;
        return url.replace("github.com/", "raw.githubusercontent.com/").replace("/blob/", "/");
    }

    private static DownloadResult downloadFile(String sourceUrl, String fallbackExt) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(sourceUrl).openConnection();
        conn.setRequestProperty("User-Agent", "saltclient-installer/1.0");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setInstanceFollowRedirects(true);

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("Download failed: HTTP " + code);
        }

        String fileName = extractFileName(conn, sourceUrl);
        String ext = extension(fileName);
        if (ext.isEmpty()) ext = fallbackExt;

        Path temp = Files.createTempFile("salt-installer-", ext);
        try (InputStream in = conn.getInputStream(); OutputStream out = Files.newOutputStream(temp)) {
            in.transferTo(out);
        }

        return new DownloadResult(temp, fileName);
    }

    private static Path installDownloaded(Path downloaded, String fileName, InstallType type) throws IOException {
        Path targetRoot = destination(type);
        Files.createDirectories(targetRoot);

        if (type == InstallType.WORLD) {
            Path folder = resolveUniqueDir(targetRoot, stripExtension(sanitizeName(fileName)));
            Files.createDirectories(folder);
            extractWorldZip(downloaded, folder);
            return folder;
        }

        String safeName = sanitizeName(fileName);
        if (!safeName.toLowerCase(Locale.ROOT).endsWith(type.preferredExt)) {
            safeName = stripExtension(safeName) + type.preferredExt;
        }

        Path target = targetRoot.resolve(safeName);
        Files.copy(downloaded, target, StandardCopyOption.REPLACE_EXISTING);
        return target;
    }

    private static void extractWorldZip(Path zipPath, Path destination) throws IOException {
        try (ZipFile zip = new ZipFile(zipPath.toFile())) {
            String commonRoot = commonRoot(zip);

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String rawName = entry.getName();
                if (rawName == null || rawName.isBlank()) continue;

                String name = rawName;
                if (commonRoot != null && name.startsWith(commonRoot + "/")) {
                    name = name.substring(commonRoot.length() + 1);
                    if (name.isBlank()) continue;
                }

                Path output = destination.resolve(name).normalize();
                if (!output.startsWith(destination)) {
                    throw new IOException("Invalid zip entry path: " + rawName);
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(output);
                    continue;
                }

                Files.createDirectories(output.getParent());
                try (InputStream in = zip.getInputStream(entry); OutputStream out = Files.newOutputStream(output)) {
                    in.transferTo(out);
                }
            }
        }
    }

    private static String commonRoot(ZipFile zip) {
        List<String> roots = new ArrayList<>();
        Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.isDirectory()) continue;

            String name = entry.getName();
            if (name == null || name.isBlank()) continue;

            int slash = name.indexOf('/');
            if (slash <= 0) {
                return null;
            }

            roots.add(name.substring(0, slash));
        }

        if (roots.isEmpty()) return null;
        String root = roots.get(0);
        for (String value : roots) {
            if (!root.equals(value)) {
                return null;
            }
        }
        return root;
    }

    private static Path resolveUniqueDir(Path parent, String baseName) {
        String safe = (baseName == null || baseName.isBlank()) ? "world" : baseName;
        Path candidate = parent.resolve(safe);
        int n = 2;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(safe + "-" + n);
            n++;
        }
        return candidate;
    }

    private static boolean matchesType(String fileName, InstallType type) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return switch (type) {
            case MOD -> lower.endsWith(".jar");
            case TEXTURE_PACK, WORLD -> lower.endsWith(".zip");
        };
    }

    private static String extension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot);
    }

    private static String stripExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0) return fileName;
        return fileName.substring(0, dot);
    }

    private static String sanitizeName(String fileName) {
        String value = fileName == null ? "download" : fileName.trim();
        if (value.isEmpty()) value = "download";
        value = value.replaceAll("[\\\\/:*?\"<>|]", "_");
        return value;
    }

    private static String extractFileName(HttpURLConnection conn, String sourceUrl) {
        String header = conn.getHeaderField("Content-Disposition");
        if (header != null) {
            Matcher m = Pattern.compile("filename\\*=UTF-8''([^;]+)|filename=\\\"?([^;\\\"]+)\\\"?").matcher(header);
            if (m.find()) {
                String v = m.group(1) != null ? m.group(1) : m.group(2);
                if (v != null && !v.isBlank()) {
                    return v.replace("%20", " ");
                }
            }
        }

        try {
            URI uri = URI.create(sourceUrl);
            String path = uri.getPath();
            if (path != null) {
                int idx = path.lastIndexOf('/');
                if (idx >= 0 && idx < path.length() - 1) {
                    return path.substring(idx + 1);
                }
            }
        } catch (Exception ignored) {
        }

        return "download";
    }

    private static JsonObject requestJsonObject(String url) throws IOException {
        JsonElement element = requestJson(url);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonArray requestJsonArray(String url) throws IOException {
        JsonElement element = requestJson(url);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

    private static JsonElement requestJson(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "saltclient-installer/1.0");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(12000);
        conn.setReadTimeout(20000);

        int code = conn.getResponseCode();
        if (code < 200 || code >= 300) {
            throw new IOException("API error: HTTP " + code);
        }

        try (InputStream in = conn.getInputStream()) {
            return JsonParser.parseReader(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
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

    private record DownloadResult(Path path, String fileName) {}
}
