package com.saltclient.audio;

import com.saltclient.mixin.SoundManagerAccessor;
import com.saltclient.mixin.SoundSystemAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.client.sound.SoundEngine;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class SongPlayerService {
    private static Channel.SourceManager currentSource;
    private static Path currentTrack;

    private SongPlayerService() {}

    public static Path musicDir() {
        return FabricLoader.getInstance().getGameDir().resolve("saltclient").resolve("music");
    }

    public static void ensureMusicDir() {
        try {
            Files.createDirectories(musicDir());
        } catch (Exception ignored) {
        }
    }

    public static List<Path> listTracks() {
        ensureMusicDir();

        List<Path> out = new ArrayList<>();
        try (Stream<Path> files = Files.list(musicDir())) {
            files.filter(Files::isRegularFile)
                .filter(SongPlayerService::isSupported)
                .sorted(Comparator.comparing(path -> path.getFileName().toString().toLowerCase(Locale.ROOT)))
                .forEach(out::add);
        } catch (Exception ignored) {
        }
        return out;
    }

    public static synchronized String play(Path file) {
        if (file == null) return "No file selected";
        if (!Files.isRegularFile(file)) return "Track file not found";
        if (!isSupported(file)) return "Only .mp3 and .ogg files are supported";

        stop();

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return "Play failed: Minecraft client not ready";

        SoundManager soundManager = mc.getSoundManager();
        SoundSystem soundSystem = ((SoundManagerAccessor) soundManager).saltclient$getSoundSystem();
        if (soundSystem == null) return "Play failed: Sound system not ready";

        Channel channel = ((SoundSystemAccessor) soundSystem).saltclient$getChannel();
        if (channel == null) return "Play failed: Sound channel not ready";

        AudioStream stream;
        try {
            stream = openStream(file);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
            return "Play failed: " + msg;
        }

        try {
            Channel.SourceManager src = channel.createSource(SoundEngine.RunMode.STREAMING).get(2, TimeUnit.SECONDS);
            if (src == null) {
                stream.close();
                return "Play failed: No available sound sources";
            }

            // All Source operations must happen on Minecraft's sound executor thread.
            src.run(source -> {
                source.setRelative(true);
                source.disableAttenuation();
                source.setVolume(1.0f);
                source.setPitch(1.0f);
                source.setLooping(false);
                source.setStream(stream);
                source.play();
            });

            currentSource = src;
            currentTrack = file;
            return "Playing: " + file.getFileName();
        } catch (Exception e) {
            try {
                stream.close();
            } catch (Exception ignored) {
            }

            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
            return "Play failed: " + msg;
        }
    }

    public static synchronized void stop() {
        if (currentSource != null) {
            Channel.SourceManager src = currentSource;
            currentSource = null;

            // Close the source on the sound executor thread.
            src.run(ignored -> src.close());
        }
        currentTrack = null;
    }

    public static synchronized boolean isPlaying() {
        if (currentSource == null) return false;
        if (currentSource.isStopped()) {
            currentSource = null;
            currentTrack = null;
            return false;
        }
        return true;
    }

    public static synchronized Path currentTrack() {
        // Keep UI in sync if the sound system has already stopped this source.
        if (currentSource == null) return null;
        if (currentSource.isStopped()) {
            currentSource = null;
            currentTrack = null;
            return null;
        }
        return currentTrack;
    }

    private static AudioStream openStream(Path file) throws IOException {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);

        InputStream raw = Files.newInputStream(file);
        BufferedInputStream in = new BufferedInputStream(raw);

        try {
            if (name.endsWith(".ogg")) return new OggAudioStream(in);
            if (name.endsWith(".mp3")) return new Mp3AudioStream(in);
        } catch (Exception e) {
            try {
                in.close();
            } catch (Exception ignored) {
            }
            throw e;
        }

        in.close();
        throw new IOException("Unsupported audio file");
    }

    private static boolean isSupported(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".ogg") || name.endsWith(".mp3");
    }
}
