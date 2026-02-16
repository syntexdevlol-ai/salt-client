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
import java.util.stream.Stream;

public final class SongPlayerService {
    private static Channel.SourceManager currentSource;
    private static TrackingAudioStream currentStream;

    // What the user wants to play (used to auto-resume across disconnects/death).
    private static boolean wantPlaying;
    private static Path wantedTrack;

    private static long lastAutoResumeMs;
    private static long resumeToken;
    private static boolean resumeInFlight;

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

        // This is now the "desired" track. We keep it so playback can auto-resume
        // if vanilla stops all sounds during server changes or respawn screens.
        wantPlaying = true;
        wantedTrack = file;

        stopCurrentSource();

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return "Play failed: Minecraft client not ready";

        SoundManager soundManager = mc.getSoundManager();
        SoundSystem soundSystem = ((SoundManagerAccessor) soundManager).saltclient$getSoundSystem();
        if (soundSystem == null) return "Play failed: Sound system not ready";

        Channel channel = ((SoundSystemAccessor) soundSystem).saltclient$getChannel();
        if (channel == null) return "Play failed: Sound channel not ready";

        TrackingAudioStream stream;
        try {
            stream = new TrackingAudioStream(openStream(file));
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
            wantPlaying = false;
            wantedTrack = null;
            return "Play failed: " + msg;
        }

        try {
            // Don't block the UI thread waiting for audio; create the source async.
            resumeToken++;
            long token = resumeToken;
            resumeInFlight = true;
            channel.createSource(SoundEngine.RunMode.STREAMING).whenComplete((src, err) -> {
                synchronized (SongPlayerService.class) {
                    resumeInFlight = false;
                }

                if (err != null || src == null) {
                    try {
                        stream.close();
                    } catch (Exception ignored) {
                    }
                    return;
                }

                synchronized (SongPlayerService.class) {
                    // If a newer play/stop happened while we were creating the source, discard.
                    if (token != resumeToken || !wantPlaying || wantedTrack == null || !wantedTrack.equals(file)) {
                        src.run(ignored -> safeClose(src));
                        try {
                            stream.close();
                        } catch (Exception ignored) {
                        }
                        return;
                    }

                    currentSource = src;
                    currentStream = stream;
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
            });

            return "Playing: " + file.getFileName();
        } catch (Exception e) {
            try {
                stream.close();
            } catch (Exception ignored) {
            }

            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
            wantPlaying = false;
            wantedTrack = null;
            return "Play failed: " + msg;
        }
    }

    public static synchronized void stop() {
        wantPlaying = false;
        wantedTrack = null;
        stopCurrentSource();
    }

    public static synchronized boolean isPlaying() {
        if (currentSource == null) return false;
        return !currentSource.isStopped();
    }

    public static synchronized Path currentTrack() {
        if (!isPlaying()) return null;
        return wantedTrack;
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

    /**
     * Called every client tick to auto-resume playback if vanilla stops all sounds
     * (ex: disconnecting, joining servers, death/respawn flows).
     */
    public static void tick(MinecraftClient mc) {
        Path track;
        boolean shouldPlay;
        Channel.SourceManager src;
        TrackingAudioStream stream;
        boolean inFlight;

        synchronized (SongPlayerService.class) {
            shouldPlay = wantPlaying;
            track = wantedTrack;
            src = currentSource;
            stream = currentStream;
            inFlight = resumeInFlight;
        }

        if (!shouldPlay || track == null || mc == null) return;
        if (inFlight) return;

        boolean stopped = (src == null) || src.isStopped();
        if (!stopped) return;

        boolean ended = stream != null && stream.isEnded();

        synchronized (SongPlayerService.class) {
            // Clear stale references.
            currentSource = null;
            currentStream = null;
        }

        // If the song ended naturally, don't loop.
        if (ended) {
            synchronized (SongPlayerService.class) {
                wantPlaying = false;
            }
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastAutoResumeMs < 1200L) return;
        lastAutoResumeMs = now;

        SoundManager soundManager = mc.getSoundManager();
        SoundSystem soundSystem;
        try {
            soundSystem = ((SoundManagerAccessor) soundManager).saltclient$getSoundSystem();
        } catch (Exception e) {
            return;
        }
        if (soundSystem == null) return;

        Channel channel;
        try {
            channel = ((SoundSystemAccessor) soundSystem).saltclient$getChannel();
        } catch (Exception e) {
            return;
        }
        if (channel == null) return;

        TrackingAudioStream newStream;
        try {
            newStream = new TrackingAudioStream(openStream(track));
        } catch (Exception e) {
            // If we can't reopen the track, stop trying.
            synchronized (SongPlayerService.class) {
                wantPlaying = false;
            }
            return;
        }

        synchronized (SongPlayerService.class) {
            // If user stopped or changed tracks since we started reopening, discard.
            if (!wantPlaying || wantedTrack == null || !wantedTrack.equals(track)) {
                try {
                    newStream.close();
                } catch (Exception ignored) {
                }
                return;
            }

            resumeToken++;
            resumeInFlight = true;
        }

        long token;
        synchronized (SongPlayerService.class) {
            token = resumeToken;
        }

        channel.createSource(SoundEngine.RunMode.STREAMING).whenComplete((newSrc, err) -> {
            synchronized (SongPlayerService.class) {
                resumeInFlight = false;
            }

            if (err != null || newSrc == null) {
                try {
                    newStream.close();
                } catch (Exception ignored) {
                }
                return;
            }

            synchronized (SongPlayerService.class) {
                if (token != resumeToken || !wantPlaying || wantedTrack == null || !wantedTrack.equals(track)) {
                    newSrc.run(ignored -> safeClose(newSrc));
                    try {
                        newStream.close();
                    } catch (Exception ignored) {
                    }
                    return;
                }

                currentSource = newSrc;
                currentStream = newStream;
            }

            newSrc.run(source -> {
                source.setRelative(true);
                source.disableAttenuation();
                source.setVolume(1.0f);
                source.setPitch(1.0f);
                source.setLooping(false);
                source.setStream(newStream);
                source.play();
            });
        });
    }

    private static synchronized void stopCurrentSource() {
        if (currentSource == null) return;

        Channel.SourceManager src = currentSource;
        currentSource = null;
        currentStream = null;

        // Close the source on the sound executor thread. Guard against double-closes.
        src.run(ignored -> safeClose(src));
    }

    private static void safeClose(Channel.SourceManager src) {
        if (src == null) return;
        try {
            if (!src.isStopped()) src.close();
        } catch (Throwable ignored) {
            // Never crash the sound thread.
        }
    }

    /**
     * Wraps an AudioStream so we can detect natural EOF vs "stopped by vanilla".
     */
    private static final class TrackingAudioStream implements AudioStream {
        private final AudioStream delegate;
        private volatile boolean ended;

        TrackingAudioStream(AudioStream delegate) {
            this.delegate = delegate;
        }

        boolean isEnded() {
            return ended;
        }

        @Override
        public javax.sound.sampled.AudioFormat getFormat() {
            return delegate.getFormat();
        }

        @Override
        public java.nio.ByteBuffer read(int size) throws IOException {
            java.nio.ByteBuffer buf = delegate.read(size);
            if (buf == null) ended = true;
            return buf;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }

    private static boolean isSupported(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".ogg") || name.endsWith(".mp3");
    }
}
