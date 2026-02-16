package com.saltclient.tweaks;

import com.saltclient.SaltClient;
import com.saltclient.util.ActivityTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.GraphicsMode;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.ChunkBuilderMode;
import net.minecraft.client.util.Window;

/**
 * "Lite" implementations of performance/visual modules by tweaking vanilla options.
 *
 * This avoids invasive rendering hooks and stays Pojav-friendly.
 */
public final class OptionTweaks {
    private static boolean baselined;
    private static int dynViewDistance;
    private static long lastDynAdjustMs;
    private static long lastApplyMs;
    private static int lastMask;

    private static int baseViewDistance;
    private static double baseEntityDistanceScaling;
    private static int baseMaxFps;
    private static CloudRenderMode baseClouds;
    private static GraphicsMode baseGraphics;
    private static boolean baseAo;
    private static ParticlesMode baseParticles;
    private static boolean baseEntityShadows;
    private static int baseMipmap;
    private static int baseBiomeBlend;
    private static int baseMenuBlur;
    private static boolean baseBobView;
    private static double baseDistortionScale;
    private static double baseDamageTilt;
    private static double baseFovEffectScale;
    private static ChunkBuilderMode baseChunkBuilder;
    private static boolean baseForceUnicodeFont;
    private static boolean baseDirectionalAudio;
    private static boolean baseShowSubtitles;
    private static boolean baseUseNativeTransport;
    private static boolean baseSprintToggled;
    private static boolean baseSneakToggled;
    private static boolean baseHudHidden;

    private OptionTweaks() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        GameOptions o = mc.options;

        if (!baselined) baseline(o);

        // If nothing relevant changed and we don't need realtime adjustments, don't spam option logic.
        long now = System.currentTimeMillis();
        int mask = computeMask();
        boolean needsRealtime =
            SaltClient.MODULES.isEnabled("dynamicresolution")
                || SaltClient.MODULES.isEnabled("dynamicfps")
                || SaltClient.MODULES.isEnabled("backgroundfpslimit")
                || SaltClient.MODULES.isEnabled("unfocusedfpssaver");
        if (!needsRealtime && mask == lastMask && (now - lastApplyMs) < 1000L) {
            return;
        }
        lastMask = mask;
        lastApplyMs = now;

        // ---- Compute target values (start from baseline) ----
        int viewDistance = baseViewDistance;
        double entityDistanceScaling = baseEntityDistanceScaling;
        int maxFps = baseMaxFps;
        CloudRenderMode clouds = baseClouds;
        GraphicsMode graphics = baseGraphics;
        boolean ao = baseAo;
        ParticlesMode particles = baseParticles;
        boolean entityShadows = baseEntityShadows;
        int mipmap = baseMipmap;
        int biomeBlend = baseBiomeBlend;
        int menuBlur = baseMenuBlur;
        boolean bobView = baseBobView;
        double distortionScale = baseDistortionScale;
        double damageTilt = baseDamageTilt;
        double fovEffectScale = baseFovEffectScale;
        ChunkBuilderMode chunkBuilder = baseChunkBuilder;
        boolean forceUnicodeFont = baseForceUnicodeFont;
        boolean directionalAudio = baseDirectionalAudio;
        boolean showSubtitles = baseShowSubtitles;
        boolean useNativeTransport = baseUseNativeTransport;
        boolean hudHidden = baseHudHidden;

        boolean lowGraphicsMode = SaltClient.MODULES.isEnabled("lowgraphicsmode");
        boolean fpsBoost = SaltClient.MODULES.isEnabled("fpsboost");

        // "Big preset" first; smaller toggles can further restrict.
        if (lowGraphicsMode) {
            graphics = GraphicsMode.FAST;
            ao = false;
            clouds = CloudRenderMode.OFF;
            entityShadows = false;
            particles = ParticlesMode.MINIMAL;
            mipmap = 0;
            biomeBlend = 0;
            viewDistance = Math.min(viewDistance, 6);
            entityDistanceScaling = Math.min(entityDistanceScaling, 0.5);
        } else if (fpsBoost) {
            ao = false;
            entityShadows = false;
            particles = ParticlesMode.DECREASED;
            clouds = CloudRenderMode.FAST;
            viewDistance = Math.min(viewDistance, Math.max(4, baseViewDistance - 2));
        }

        // Individual toggles
        if (SaltClient.MODULES.isEnabled("clouddisabler")) clouds = CloudRenderMode.OFF;
        if (SaltClient.MODULES.isEnabled("shadowdisabler")) entityShadows = false;
        if (SaltClient.MODULES.isEnabled("particlereducer")) particles = ParticlesMode.MINIMAL;
        if (SaltClient.MODULES.isEnabled("fastlighting")) ao = false;
        if (SaltClient.MODULES.isEnabled("entityculling")) entityDistanceScaling = Math.min(entityDistanceScaling, 0.5);
        if (SaltClient.MODULES.isEnabled("chunkculling")) viewDistance = Math.min(viewDistance, Math.max(2, baseViewDistance - 4));
        if (SaltClient.MODULES.isEnabled("blockculling")) biomeBlend = 0;

        if (SaltClient.MODULES.isEnabled("mipmapoptimizer") || SaltClient.MODULES.isEnabled("textureoptimizer")) mipmap = 0;
        if (SaltClient.MODULES.isEnabled("textureoptimizer")) biomeBlend = 0;

        if (SaltClient.MODULES.isEnabled("uiblurtoggle")) menuBlur = 0;

        if (SaltClient.MODULES.isEnabled("animationlimiter")) {
            bobView = false;
            distortionScale = 0.0;
            damageTilt = 0.0;
        }

        if (SaltClient.MODULES.isEnabled("fastmath")) {
            // Not truly "fast math" (vanilla doesn't expose a switch), but this reduces a few effect calculations.
            fovEffectScale = 0.0;
            distortionScale = 0.0;
        }

        if (SaltClient.MODULES.isEnabled("threadoptimizer") || SaltClient.MODULES.isEnabled("smartrendering")) {
            chunkBuilder = ChunkBuilderMode.NEARBY;
        }

        if (SaltClient.MODULES.isEnabled("soundengineoptimizer")) {
            directionalAudio = false;
            showSubtitles = false;
        }

        if (SaltClient.MODULES.isEnabled("networkoptimizer")) {
            useNativeTransport = true;
        }

        if (SaltClient.MODULES.isEnabled("cleanhud")) {
            hudHidden = true;
        }

        // DynamicResolution (lite): adjusts view distance based on FPS.
        viewDistance = applyDynamicResolution(mc, viewDistance);

        set(o.getSprintToggled(), SaltClient.MODULES.isEnabled("togglesprint") ? true : baseSprintToggled);
        set(o.getSneakToggled(), SaltClient.MODULES.isEnabled("togglesneak") ? true : baseSneakToggled);

        // Framerate limit modules: choose the lowest limit requested.
        maxFps = applyFpsLimiters(mc, maxFps);

        // ---- Apply target values ----
        set(o.getViewDistance(), viewDistance);
        set(o.getEntityDistanceScaling(), entityDistanceScaling);

        applyFps(o, mc.getWindow(), maxFps);

        set(o.getCloudRenderMode(), clouds);
        set(o.getGraphicsMode(), graphics);
        set(o.getAo(), ao);
        set(o.getParticles(), particles);
        set(o.getEntityShadows(), entityShadows);
        set(o.getMipmapLevels(), mipmap);
        set(o.getBiomeBlendRadius(), biomeBlend);
        set(o.getMenuBackgroundBlurriness(), menuBlur);
        set(o.getBobView(), bobView);
        set(o.getDistortionEffectScale(), distortionScale);
        set(o.getDamageTiltStrength(), damageTilt);
        set(o.getFovEffectScale(), fovEffectScale);
        set(o.getChunkBuilderMode(), chunkBuilder);

        // FontRenderer: force unicode font (visible change). We don't auto-toggle each tick because it can trigger reloads.
        if (!SaltClient.MODULES.isEnabled("fontrenderer")) {
            // Restore to baseline if user disables the module.
            set(o.getForceUnicodeFont(), forceUnicodeFont);
        }

        set(o.getDirectionalAudio(), directionalAudio);
        set(o.getShowSubtitles(), showSubtitles);

        o.useNativeTransport = useNativeTransport;
        o.hudHidden = hudHidden;
    }

    private static int computeMask() {
        int m = 0;
        m |= bit(0, SaltClient.MODULES.isEnabled("lowgraphicsmode"));
        m |= bit(1, SaltClient.MODULES.isEnabled("fpsboost"));
        m |= bit(2, SaltClient.MODULES.isEnabled("clouddisabler"));
        m |= bit(3, SaltClient.MODULES.isEnabled("shadowdisabler"));
        m |= bit(4, SaltClient.MODULES.isEnabled("particlereducer"));
        m |= bit(5, SaltClient.MODULES.isEnabled("fastlighting"));
        m |= bit(6, SaltClient.MODULES.isEnabled("entityculling"));
        m |= bit(7, SaltClient.MODULES.isEnabled("chunkculling"));
        m |= bit(8, SaltClient.MODULES.isEnabled("blockculling"));
        m |= bit(9, SaltClient.MODULES.isEnabled("mipmapoptimizer"));
        m |= bit(10, SaltClient.MODULES.isEnabled("textureoptimizer"));
        m |= bit(11, SaltClient.MODULES.isEnabled("uiblurtoggle"));
        m |= bit(12, SaltClient.MODULES.isEnabled("animationlimiter"));
        m |= bit(13, SaltClient.MODULES.isEnabled("fastmath"));
        m |= bit(14, SaltClient.MODULES.isEnabled("threadoptimizer"));
        m |= bit(15, SaltClient.MODULES.isEnabled("smartrendering"));
        m |= bit(16, SaltClient.MODULES.isEnabled("soundengineoptimizer"));
        m |= bit(17, SaltClient.MODULES.isEnabled("networkoptimizer"));
        m |= bit(18, SaltClient.MODULES.isEnabled("cleanhud"));
        m |= bit(19, SaltClient.MODULES.isEnabled("togglesprint"));
        m |= bit(20, SaltClient.MODULES.isEnabled("togglesneak"));
        m |= bit(21, SaltClient.MODULES.isEnabled("dynamicresolution"));
        m |= bit(22, SaltClient.MODULES.isEnabled("dynamicfps"));
        m |= bit(23, SaltClient.MODULES.isEnabled("backgroundfpslimit"));
        m |= bit(24, SaltClient.MODULES.isEnabled("unfocusedfpssaver"));
        m |= bit(25, SaltClient.MODULES.isEnabled("fontrenderer"));
        return m;
    }

    private static int bit(int idx, boolean enabled) {
        return enabled ? (1 << idx) : 0;
    }

    private static int applyDynamicResolution(MinecraftClient mc, int viewDistanceLimit) {
        if (!SaltClient.MODULES.isEnabled("dynamicresolution")) {
            dynViewDistance = viewDistanceLimit;
            return viewDistanceLimit;
        }

        long now = System.currentTimeMillis();
        if (dynViewDistance <= 0) dynViewDistance = viewDistanceLimit;

        // Adjust at most once per second to avoid constant world rebuild churn.
        if (now - lastDynAdjustMs >= 1000L) {
            lastDynAdjustMs = now;
            int fps = mc.getCurrentFps();
            if (fps > 0 && fps < 35) dynViewDistance = Math.max(2, dynViewDistance - 1);
            else if (fps > 60) dynViewDistance = dynViewDistance + 1;
        }

        if (dynViewDistance > viewDistanceLimit) dynViewDistance = viewDistanceLimit;
        return dynViewDistance;
    }

    private static int applyFpsLimiters(MinecraftClient mc, int baseline) {
        int limit = baseline;

        if (SaltClient.MODULES.isEnabled("backgroundfpslimit") && !mc.isWindowFocused()) {
            limit = Math.min(limit, 15);
        }

        if (SaltClient.MODULES.isEnabled("unfocusedfpssaver") && mc.currentScreen != null) {
            limit = Math.min(limit, 30);
        }

        if (SaltClient.MODULES.isEnabled("dynamicfps")) {
            boolean idle = ActivityTracker.idleMs() > 2500L;
            if (idle && mc.currentScreen == null && mc.player != null) {
                limit = Math.min(limit, 30);
            }
        }

        return limit;
    }

    private static void applyFps(GameOptions o, Window w, int target) {
        if (target < 1) target = 1;
        int current = o.getMaxFps().getValue();
        if (current != target) {
            o.getMaxFps().setValue(target);
            w.setFramerateLimit(target);
        }
    }

    private static void baseline(GameOptions o) {
        baseViewDistance = o.getViewDistance().getValue();
        baseEntityDistanceScaling = o.getEntityDistanceScaling().getValue();
        baseMaxFps = o.getMaxFps().getValue();
        baseClouds = o.getCloudRenderMode().getValue();
        baseGraphics = o.getGraphicsMode().getValue();
        baseAo = o.getAo().getValue();
        baseParticles = o.getParticles().getValue();
        baseEntityShadows = o.getEntityShadows().getValue();
        baseMipmap = o.getMipmapLevels().getValue();
        baseBiomeBlend = o.getBiomeBlendRadius().getValue();
        baseMenuBlur = o.getMenuBackgroundBlurriness().getValue();
        baseBobView = o.getBobView().getValue();
        baseDistortionScale = o.getDistortionEffectScale().getValue();
        baseDamageTilt = o.getDamageTiltStrength().getValue();
        baseFovEffectScale = o.getFovEffectScale().getValue();
        baseChunkBuilder = o.getChunkBuilderMode().getValue();
        baseForceUnicodeFont = o.getForceUnicodeFont().getValue();
        baseDirectionalAudio = o.getDirectionalAudio().getValue();
        baseShowSubtitles = o.getShowSubtitles().getValue();
        baseUseNativeTransport = o.useNativeTransport;
        baseSprintToggled = o.getSprintToggled().getValue();
        baseSneakToggled = o.getSneakToggled().getValue();
        baseHudHidden = o.hudHidden;
        baselined = true;
    }

    private static <T> void set(SimpleOption<T> opt, T value) {
        if (opt == null) return;
        T cur = opt.getValue();
        if (cur == value || (cur != null && cur.equals(value))) return;
        opt.setValue(value);
    }
}
