package com.saltclient.audio;

import net.fabricmc.loader.api.FabricLoader;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public final class SongPlayerService {
    private static Clip clip;
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

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file.toFile())) {
            Clip c = AudioSystem.getClip();
            c.open(stream);
            c.start();
            clip = c;
            currentTrack = file;
            return "Playing: " + file.getFileName();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = "Audio format is not supported on this JVM";
            }
            return "Play failed: " + msg;
        }
    }

    public static synchronized void stop() {
        if (clip != null) {
            try {
                clip.stop();
                clip.close();
            } catch (Exception ignored) {
            }
            clip = null;
        }
        currentTrack = null;
    }

    public static synchronized boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public static synchronized Path currentTrack() {
        return currentTrack;
    }

    private static boolean isSupported(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".ogg") || name.endsWith(".mp3");
    }
}
