package com.saltclient.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

public final class PanoramaManager {
    private static final long RESCAN_MS = 3000L;

    private static Path loadedFile;
    private static Identifier textureId;
    private static NativeImageBackedTexture texture;
    private static long nextScan;

    private PanoramaManager() {}

    public static Path panoramaDir() {
        return FabricLoader.getInstance().getGameDir().resolve("saltclient").resolve("panoramas");
    }

    public static Identifier currentTexture() {
        ensureDirectory();

        long now = System.currentTimeMillis();
        if (now < nextScan) return textureId;
        nextScan = now + RESCAN_MS;

        Path candidate = firstPanoramaFile();
        if (candidate == null) return textureId;
        if (candidate.equals(loadedFile) && textureId != null) return textureId;

        loadTexture(candidate);
        return textureId;
    }

    private static void ensureDirectory() {
        try {
            Files.createDirectories(panoramaDir());
        } catch (Exception ignored) {
        }
    }

    private static Path firstPanoramaFile() {
        Path dir = panoramaDir();
        if (!Files.isDirectory(dir)) return null;

        try (Stream<Path> files = Files.list(dir)) {
            return files.filter(Files::isRegularFile)
                .filter(PanoramaManager::isImage)
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                .findFirst()
                .orElse(null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isImage(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
    }

    private static void loadTexture(Path file) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getTextureManager() == null) return;

        try (InputStream in = Files.newInputStream(file)) {
            NativeImage image = NativeImage.read(in);
            NativeImageBackedTexture next = new NativeImageBackedTexture(image);

            Identifier id = mc.getTextureManager().registerDynamicTexture("salt_panorama", next);

            if (texture != null) {
                try {
                    texture.close();
                } catch (Exception ignored) {
                }
            }

            texture = next;
            textureId = id;
            loadedFile = file;
        } catch (Exception ignored) {
            // Keep previous texture if loading the new one failed.
        }
    }
}
