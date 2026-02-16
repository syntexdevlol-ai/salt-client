package com.saltclient.module.impl.visual;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.IntSetting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.util.math.MathHelper;

/**
 * Motion blur adapted from classic framebuffer-accumulation implementations.
 *
 * This matches the behavior from older Forge clients:
 * - Each frame is blended with the previous blurred frame.
 * - Amount controls trailing strength.
 */
public final class MotionBlurModule extends Module {
    private final IntSetting amount;
    private SimpleFramebuffer blurBufferMain;
    private SimpleFramebuffer blurBufferInto;
    private int bufferWidth = -1;
    private int bufferHeight = -1;

    public MotionBlurModule() {
        super("motionblur", "MotionBlur", "Adds smooth camera motion blur.", ModuleCategory.VISUAL, true);
        this.amount = addSetting(new IntSetting("amount", "Amount", "Higher = stronger blur trail.", 7, 2, 10, 1));
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        releaseBuffers();
    }

    @Override
    public void onHudRender(net.minecraft.client.gui.DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        if (mc.currentScreen != null) return;

        Framebuffer main = mc.getFramebuffer();
        if (main == null || main.textureWidth <= 0 || main.textureHeight <= 0) return;

        if (!ensureBuffers(main.textureWidth, main.textureHeight)) return;

        // Compose new blurred frame:
        // into = current + previous * alpha
        blurBufferInto.clear(MinecraftClient.IS_SYSTEM_MAC);
        blurBufferInto.beginWrite(true);

        main.draw(main.textureWidth, main.textureHeight, true);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, blurAlpha());
        blurBufferMain.draw(main.textureWidth, main.textureHeight, false);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();

        // Output composed frame back to main framebuffer.
        main.beginWrite(true);
        blurBufferInto.draw(main.textureWidth, main.textureHeight, true);
        main.endWrite();

        // Swap framebuffers for next frame accumulation.
        SimpleFramebuffer tmp = blurBufferMain;
        blurBufferMain = blurBufferInto;
        blurBufferInto = tmp;
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        releaseBuffers();
    }

    private boolean ensureBuffers(int width, int height) {
        if (blurBufferMain != null && blurBufferInto != null && width == bufferWidth && height == bufferHeight) {
            return true;
        }

        try {
            releaseBuffers();
            blurBufferMain = createBuffer(width, height);
            blurBufferInto = createBuffer(width, height);
            bufferWidth = width;
            bufferHeight = height;
            return true;
        } catch (Throwable ignored) {
            releaseBuffers();
            return false;
        }
    }

    private static SimpleFramebuffer createBuffer(int width, int height) {
        SimpleFramebuffer buffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
        buffer.setTexFilter(9728); // GL_NEAREST
        buffer.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        buffer.clear(MinecraftClient.IS_SYSTEM_MAC);
        return buffer;
    }

    private void releaseBuffers() {
        if (blurBufferMain != null) {
            blurBufferMain.delete();
            blurBufferMain = null;
        }
        if (blurBufferInto != null) {
            blurBufferInto.delete();
            blurBufferInto = null;
        }
        bufferWidth = -1;
        bufferHeight = -1;
    }

    private float blurAlpha() {
        // Ported from the old implementation: amount/10 - 0.1
        float v = (amount.getValue() / 10.0F) - 0.1F;
        return MathHelper.clamp(v, 0.0F, 0.92F);
    }
}
