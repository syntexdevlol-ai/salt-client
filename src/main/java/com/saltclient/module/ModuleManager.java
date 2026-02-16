package com.saltclient.module;

import com.saltclient.module.impl.hud.*;
import com.saltclient.module.impl.combat.HitColorModule;
import com.saltclient.module.impl.camera.FreeLookModule;
import com.saltclient.module.impl.camera.PerspectiveModule;
import com.saltclient.module.impl.crosshair.CrosshairEditorModule;
import com.saltclient.module.impl.crosshair.CustomCrosshairModule;
import com.saltclient.module.impl.movement.AutoSprintModule;
import com.saltclient.module.impl.movement.InventoryWalkModule;
import com.saltclient.module.impl.misc.ReplayIndicatorModule;
import com.saltclient.module.impl.misc.ScreenshotHelperModule;
import com.saltclient.module.impl.performance.FontRendererModule;
import com.saltclient.module.impl.performance.RamCleanerModule;
import com.saltclient.module.impl.visual.FullBrightModule;
import com.saltclient.module.impl.visual.MotionBlurModule;
import com.saltclient.module.impl.visual.ZoomModule;
import com.saltclient.util.HudLayout;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> byId = new HashMap<>();

    public List<Module> all() {
        return Collections.unmodifiableList(modules);
    }

    public Optional<Module> byId(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public boolean isEnabled(String id) {
        Module m = byId.get(id);
        return m != null && m.isEnabled();
    }

    public void register(Module module) {
        if (byId.containsKey(module.getId())) {
            throw new IllegalStateException("Duplicate module id: " + module.getId());
        }
        modules.add(module);
        byId.put(module.getId(), module);
    }

    public void onTick(MinecraftClient mc) {
        for (Module m : modules) {
            m.onBindTick(mc);
            if (m.isEnabled()) m.onTick(mc);
        }
    }

    public void onHudRender(DrawContext ctx) {
        HudLayout.beginFrame(MinecraftClient.getInstance());
        HudPos.beginFrame();
        for (Module m : modules) {
            if (m.isEnabled()) m.onHudRender(ctx);
        }
    }

    /**
     * Register the requested module list.
     */
    public void registerDefaults() {
        // HUD
        register(new KeystrokesModule());
        register(new MouseButtonsModule());
        register(new CpsCounterModule());
        register(new LeftClickCpsModule());
        register(new RightClickCpsModule());
        register(new FpsCounterModule());
        register(new PingCounterModule());
        register(new ServerTpsModule());
        register(new ComboCounterModule());
        register(new ReachDisplayModule());
        register(new TargetHudModule());
        register(new ArmorStatusModule());
        register(new PotionHudModule());
        register(new CoordinatesModule());
        register(new DirectionHudModule());
        register(new SessionTimeModule());
        register(new PlayerHudModule());
        register(new ToggleModule("minimalhud", "MinimalHUD", "Simplify vanilla HUD.", ModuleCategory.HUD));
        register(new ToggleModule("cleanhud", "CleanHUD", "Hide vanilla HUD.", ModuleCategory.HUD));
        register(new HudEditorModule());

        // Chat
        register(new ToggleModule("chattimestamp", "ChatTimestamp", "Prefix chat with timestamps.", ModuleCategory.CHAT));
        register(new ToggleModule("chatcleaner", "ChatCleaner", "Deduplicate some chat spam.", ModuleCategory.CHAT));
        register(new ToggleModule("chatautogg", "ChatAutoGG", "Auto-send gg (best-effort).", ModuleCategory.CHAT));

        // Misc
        register(new ScreenshotHelperModule());
        register(new ReplayIndicatorModule());

        // Camera / visual
        register(new PerspectiveModule());
        register(new FreeLookModule());
        register(new ZoomModule());
        register(new ToggleModule("zoomscroll", "ZoomScroll", "Adjust zoom with scroll.", ModuleCategory.CAMERA));
        register(new FullBrightModule());
        register(new MotionBlurModule());

        // Crosshair
        register(new CrosshairEditorModule());
        register(new CustomCrosshairModule());

        // Combat feedback
        register(new HitColorModule());
        register(new ToggleModule("hitsound", "HitSound", "Play a sound on hit (client-side).", ModuleCategory.COMBAT));
        register(new ToggleModule("killsound", "KillSound", "Play a sound on kill (best-effort).", ModuleCategory.COMBAT));

        // Visual
        register(new ToggleModule("lowfire", "LowFire", "Disable fire overlay.", ModuleCategory.VISUAL));
        register(new ToggleModule("clearwater", "ClearWater", "Disable underwater overlay + reduce water fog.", ModuleCategory.VISUAL));
        register(new ToggleModule("timechanger", "TimeChanger", "Client-side time override.", ModuleCategory.VISUAL));
        register(new ToggleModule("weatherchanger", "WeatherChanger", "Client-side weather override.", ModuleCategory.VISUAL));
        register(new ToggleModule("weatherdisabler", "WeatherDisabler", "Disable client-side weather.", ModuleCategory.VISUAL));
        register(new ToggleModule("fogremover", "FogRemover", "Reduce fog.", ModuleCategory.VISUAL));
        register(new ToggleModule("clouddisabler", "CloudDisabler", "Disable clouds.", ModuleCategory.VISUAL));
        register(new ToggleModule("shadowdisabler", "ShadowDisabler", "Disable entity shadows.", ModuleCategory.VISUAL));

        // Performance (lite via option tweaks)
        register(new ToggleModule("animationlimiter", "AnimationLimiter", "Limit some animations.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("particlereducer", "ParticleReducer", "Reduce particles.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("entityculling", "EntityCulling", "Reduce entity render distance (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("blockculling", "BlockCulling", "Reduce biome blend radius (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("chunkculling", "ChunkCulling", "Reduce view distance (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("smartrendering", "SmartRendering", "Smart rendering tweaks (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("dynamicfps", "DynamicFPS", "Reduce FPS when idle (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("backgroundfpslimit", "BackgroundFPSLimit", "Limit FPS when unfocused.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("fpsboost", "FPSBoost", "Preset performance tweaks.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("fastlighting", "FastLighting", "Disable ambient occlusion.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("fastmath", "FastMath", "Reduce some effects calculations (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("lowgraphicsmode", "LowGraphicsMode", "Low graphics preset.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("hudcache", "HUDCache", "Cache HUD strings (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("uiblurtoggle", "UIBlurToggle", "Disable UI blur.", ModuleCategory.PERFORMANCE));
        register(new FontRendererModule());
        register(new ToggleModule("textureoptimizer", "TextureOptimizer", "Texture-related option tweaks (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("mipmapoptimizer", "MipmapOptimizer", "Disable mipmaps (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("dynamicresolution", "DynamicResolution", "Adjust view distance based on FPS (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("unfocusedfpssaver", "UnfocusedFPSSaver", "Limit FPS in menus (lite).", ModuleCategory.PERFORMANCE));
        register(new RamCleanerModule());
        register(new ToggleModule("gcoptimizer", "GCOptimizer", "Auto-GC under memory pressure (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("threadoptimizer", "ThreadOptimizer", "Chunk builder tweaks (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("networkoptimizer", "NetworkOptimizer", "Enable native transport when possible.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("soundengineoptimizer", "SoundEngineOptimizer", "Disable some audio features (lite).", ModuleCategory.PERFORMANCE));

        // Movement
        register(new InventoryWalkModule());
        register(new ToggleModule("togglesprint", "ToggleSprint", "Enable vanilla toggle sprint option.", ModuleCategory.MOVEMENT));
        register(new AutoSprintModule());
        register(new ToggleModule("togglesneak", "ToggleSneak", "Enable vanilla toggle sneak option.", ModuleCategory.MOVEMENT));
        register(new ToggleModule("quickdrop", "QuickDrop", "Drop full stacks without CTRL.", ModuleCategory.MOVEMENT));

    }
}
