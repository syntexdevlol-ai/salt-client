package com.saltclient.module.impl.visual;

import com.saltclient.mixin.GameRendererInvoker;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;

/**
 * Motion blur (lite):
 * - Reuses vanilla blur post shader.
 * - Applies only when no other post shader is active.
 */
public final class MotionBlurModule extends Module {
    private static final Identifier BLUR_SHADER = Identifier.of("minecraft", "shaders/post/blur.json");

    private PostEffectProcessor appliedProcessor;

    public MotionBlurModule() {
        super("motionblur", "MotionBlur", "Adds a subtle motion blur effect.", ModuleCategory.VISUAL, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        tryApply(mc);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc == null || mc.gameRenderer == null) return;

        // If another effect replaced our shader (or the game removed it), drop our reference.
        if (appliedProcessor != null && mc.gameRenderer.getPostProcessor() != appliedProcessor) {
            appliedProcessor = null;
        }

        // Re-apply only when no other post effect is running.
        if (appliedProcessor == null && mc.gameRenderer.getPostProcessor() == null) {
            tryApply(mc);
        }
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        if (mc != null && mc.gameRenderer != null && appliedProcessor != null && mc.gameRenderer.getPostProcessor() == appliedProcessor) {
            mc.gameRenderer.disablePostProcessor();
        }
        appliedProcessor = null;
    }

    private void tryApply(MinecraftClient mc) {
        if (mc == null || mc.gameRenderer == null) return;
        if (mc.gameRenderer.getPostProcessor() != null) return;

        try {
            ((GameRendererInvoker) mc.gameRenderer).saltclient$loadPostProcessor(BLUR_SHADER);
            appliedProcessor = mc.gameRenderer.getPostProcessor();
        } catch (Throwable ignored) {
            // Keep this fail-safe: if loading fails on a specific device, the module simply does nothing.
            appliedProcessor = null;
        }
    }
}
