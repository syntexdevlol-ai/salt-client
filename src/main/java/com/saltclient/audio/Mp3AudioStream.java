package com.saltclient.audio;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import net.minecraft.client.sound.AudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Minimal MP3 -> PCM decoder for the built-in song player.
 *
 * <p>Why this exists:
 * - Java Sound (AudioSystem/Clip) usually doesn't support MP3 on Android/FCL/Pojav.
 * - Minecraft's sound engine expects an {@link AudioStream} that outputs 16-bit PCM.
 *
 * <p>This uses JLayer (pure Java) to decode MP3 frames into signed 16-bit little-endian PCM.
 */
public final class Mp3AudioStream implements AudioStream, Closeable {
    private final InputStream in;
    private final Bitstream bitstream;
    private final Decoder decoder;

    private AudioFormat format;

    // Current decoded PCM frame (interleaved shorts).
    private short[] frame;
    private int framePos;
    private int frameLen;

    private boolean eof;

    public Mp3AudioStream(InputStream input) throws IOException {
        // JLayer performs a lot of small reads; buffering matters.
        this.in = (input instanceof BufferedInputStream) ? input : new BufferedInputStream(input);
        this.bitstream = new Bitstream(this.in);
        this.decoder = new Decoder();

        // Prime: decode the first frame so we can expose a correct AudioFormat.
        decodeNextFrame();
        if (this.format == null) {
            throw new IOException("Invalid MP3: no audio frames found");
        }
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    @Override
    public ByteBuffer read(int size) throws IOException {
        if (eof) return null;

        // Minecraft expects 16-bit PCM, so always write an even number of bytes.
        int targetBytes = Math.max(0, size) & ~1;
        if (targetBytes == 0) {
            // Return an empty buffer rather than null (null means EOF).
            return ByteBuffer.allocateDirect(0);
        }

        ByteBuffer out = ByteBuffer.allocateDirect(targetBytes).order(ByteOrder.LITTLE_ENDIAN);

        while (out.remaining() >= 2) {
            if (frame == null || framePos >= frameLen) {
                if (!decodeNextFrame()) break;
            }

            while (framePos < frameLen && out.remaining() >= 2) {
                out.putShort(frame[framePos++]);
            }
        }

        out.flip();
        if (!out.hasRemaining() && eof) return null;
        return out;
    }

    private boolean decodeNextFrame() throws IOException {
        if (eof) return false;

        try {
            Header header = bitstream.readFrame();
            if (header == null) {
                eof = true;
                return false;
            }

            SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);
            bitstream.closeFrame();

            this.frame = output.getBuffer();
            this.framePos = 0;
            this.frameLen = output.getBufferLength();

            if (this.format == null) {
                int channels = output.getChannelCount();
                int sampleRate = output.getSampleFrequency();
                this.format = new AudioFormat(sampleRate, 16, channels, true, false);
            }

            return true;
        } catch (BitstreamException | DecoderException e) {
            throw new IOException("MP3 decode failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            bitstream.close();
        } catch (BitstreamException ignored) {
        }
        in.close();
    }
}

