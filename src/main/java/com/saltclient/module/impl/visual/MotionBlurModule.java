package com.saltclient.module.impl.visual;

import com.saltclient.mixin.GameRendererInvoker;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

/**
 * Motion blur tuned for PvP:
 * - Uses a lighter custom blur shader.
 * - Enables only briefly when camera rotates.
 */
public final class MotionBlurModule extends Module {
    private static final Identifier BLUR_SHADER = Identifier.of("saltclient", "shaders/post/motion_blur.json");
    private static final int HOLD_TICKS = 3;
    private static final float TURN_THRESHOLD = 0.35F;

    private PostEffectProcessor appliedProcessor;
    private boolean hasLastAngles;
    private float lastYaw;
    private float lastPitch;
    private int activeTicks;

    public MotionBlurModule() {
        super("motionblur", "MotionBlur", "Adds a subtle motion blur effect.", ModuleCategory.VISUAL, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        activeTicks = 0;
        hasLastAngles = false;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc == null || mc.gameRenderer == null) return;

        if (mc.player != null) {
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();

            if (hasLastAngles) {
                float yawDelta = Math.abs(MathHelper.wrapDegrees(yaw - lastYaw));
                float pitchDelta = Math.abs(MathHelper.wrapDegrees(pitch - lastPitch));
                float turnDelta = yawDelta + (pitchDelta * 0.8F);
                if (turnDelta > TURN_THRESHOLD) {
                    activeTicks = HOLD_TICKS;
                }
            } else {
                hasLastAngles = true;
            }

            lastYaw = yaw;
            lastPitch = pitch;
        }

        if (appliedProcessor != null && mc.gameRenderer.getPostProcessor() != appliedProcessor) {
            appliedProcessor = null;
        }

        boolean shouldBlur = mc.currentScreen == null && activeTicks > 0;
        if (shouldBlur) {
            if (appliedProcessor == null && mc.gameRenderer.getPostProcessor() == null) {
                tryApply(mc);
            }
        } else {
            disableIfOwned(mc);
        }

        if (activeTicks > 0) {
            activeTicks--;
        }
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        disableIfOwned(mc);
        hasLastAngles = false;
        activeTicks = 0;
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

    private void disableIfOwned(MinecraftClient mc) {
        if (mc != null && mc.gameRenderer != null && appliedProcessor != null && mc.gameRenderer.getPostProcessor() == appliedProcessor) {
            mc.gameRenderer.disablePostProcessor();
        }
        appliedProcessor = null;
    }
}
