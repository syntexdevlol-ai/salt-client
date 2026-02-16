package com.saltclient.module;

import com.saltclient.module.impl.hud.*;
import com.saltclient.module.impl.combat.HitColorModule;
import com.saltclient.module.impl.camera.FreeLookModule;
import com.saltclient.module.impl.camera.PerspectiveModule;
import com.saltclient.module.impl.crosshair.CrosshairEditorModule;
import com.saltclient.module.impl.crosshair.CustomCrosshairModule;
import com.saltclient.module.impl.movement.AutoSprintModule;
import com.saltclient.module.impl.movement.InventoryWalkModule;
import com.saltclient.module.impl.misc.AutoRespawnModule;
import com.saltclient.module.impl.misc.GuiModule;
import com.saltclient.module.impl.misc.FontSelectorModule;
import com.saltclient.module.impl.misc.AsmrKeyboardModule;
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
        register(new KeyOverlayModule());
        register(new MouseButtonsModule());
        register(new MouseCpsGraphModule());
        register(new ClickHeatmapModule());
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
        register(new MatchTimerModule());
        register(new PlayerHudModule());
        register(new KillCounterModule());
        register(new DeathCounterModule());
        register(new StreakCounterModule());
        register(new RespawnTimerModule());
        register(new TabListPlayerCountModule());
        register(new PerformanceGraphModule());
        register(new MemoryUsageHudModule());
        register(new CpuTempHudModule());
        register(new GpuTempHudModule());
        register(new ToggleModule("damageindicator", "DamageIndicator", "Show damage feedback markers (lite).", ModuleCategory.HUD));
        register(new ToggleModule("noscoreboard", "NoScoreboard", "Hide scoreboard sidebar.", ModuleCategory.HUD));
        register(new ToggleModule("scoreboardmover", "ScoreboardMover", "Move scoreboard position (planned).", ModuleCategory.HUD));
        register(new ToggleModule("scoreboardcleaner", "ScoreboardCleaner", "Clean scoreboard text (planned).", ModuleCategory.HUD));
        register(new ToggleModule("tablistcleaner", "TabListCleaner", "Clean tab list formatting (planned).", ModuleCategory.HUD));
        register(new ToggleModule("tablistscroller", "TabListScroller", "Scrollable tab list (planned).", ModuleCategory.HUD));
        register(new ToggleModule("tablistsearch", "TabListSearch", "Search players in tab list (planned).", ModuleCategory.HUD));
        register(new ToggleModule("tablistping", "TabListPing", "Show ping in tab list (planned).", ModuleCategory.HUD));
        register(new ToggleModule("tabliststats", "TabListStats", "Show extra player stats in tab list (planned).", ModuleCategory.HUD));
        register(new ToggleModule("debugoverlay", "DebugOverlay", "Client debug overlay tools (planned).", ModuleCategory.HUD));
        register(new ToggleModule("hudsnapgrid", "HUDSnapGrid", "Snap HUD editor movement to a grid (planned).", ModuleCategory.HUD));
        register(new ToggleModule("minimalhud", "MinimalHUD", "Simplify vanilla HUD.", ModuleCategory.HUD));
        register(new ToggleModule("cleanhud", "CleanHUD", "Hide vanilla HUD.", ModuleCategory.HUD));
        register(new HudScaleModule());
        register(new HudEditorModule());

        // Chat
        register(new ToggleModule("chattimestamp", "ChatTimestamp", "Prefix chat with timestamps.", ModuleCategory.CHAT));
        register(new ToggleModule("chatcleaner", "ChatCleaner", "Deduplicate some chat spam.", ModuleCategory.CHAT));
        register(new ToggleModule("chatautogg", "ChatAutoGG", "Auto-send gg (best-effort).", ModuleCategory.CHAT));

        // Misc
        register(new GuiModule());
        register(new FontSelectorModule());
        register(new AsmrKeyboardModule());
        register(new AutoRespawnModule());
        register(new ToggleModule("serverswitcher", "ServerSwitcher", "Quick switch between saved servers (planned).", ModuleCategory.MISC));
        register(new ToggleModule("serverautoreconnect", "ServerAutoReconnect", "Auto reconnect after disconnect (planned).", ModuleCategory.MISC));
        register(new ToggleModule("disconnectnotifier", "DisconnectNotifier", "Notify when disconnected (planned).", ModuleCategory.MISC));
        register(new ToggleModule("afkdetector", "AFKDetector", "Detect AFK state from idle input (planned).", ModuleCategory.MISC));
        register(new ToggleModule("keybindmanager", "KeybindManager", "Manage module keybind groups (planned).", ModuleCategory.MISC));
        register(new ToggleModule("modulesearch", "ModuleSearch", "Enhanced module search options (planned).", ModuleCategory.MISC));
        register(new ToggleModule("modulefavorites", "ModuleFavorites", "Pin favorite modules (planned).", ModuleCategory.MISC));
        register(new ToggleModule("moduleprofiles", "ModuleProfiles", "Store module on/off profiles (planned).", ModuleCategory.MISC));
        register(new ToggleModule("profileswitcher", "ProfileSwitcher", "Switch module profiles quickly (planned).", ModuleCategory.MISC));
        register(new ToggleModule("configcloudsync", "ConfigCloudSync", "Sync configs to cloud storage (planned).", ModuleCategory.MISC));
        register(new ToggleModule("autoconfigsave", "AutoConfigSave", "Auto-save config on changes.", ModuleCategory.MISC));
        register(new ToggleModule("configversioning", "ConfigVersioning", "Keep historical config snapshots (planned).", ModuleCategory.MISC));
        register(new ToggleModule("themeswitcher", "ThemeSwitcher", "Switch UI themes (planned).", ModuleCategory.MISC));
        register(new ToggleModule("accentcolorpicker", "AccentColorPicker", "Pick GUI accent color (planned).", ModuleCategory.MISC));
        register(new ToggleModule("uiscale", "UIScale", "Adjust custom UI scale (planned).", ModuleCategory.MISC));
        register(new ToggleModule("draganddropui", "DragAndDropUI", "Drag and drop UI widgets (planned).", ModuleCategory.MISC));
        register(new ScreenshotHelperModule());
        register(new ReplayIndicatorModule());

        // Camera / visual
        register(new PerspectiveModule());
        register(new FreeLookModule());
        register(new ZoomModule());
        register(new ToggleModule("zoomscroll", "ZoomScroll", "Adjust zoom with scroll.", ModuleCategory.CAMERA));
        register(new ToggleModule("nobobview", "NoBobView", "Disable view bobbing.", ModuleCategory.CAMERA));
        register(new ToggleModule("mousesmoothingtoggle", "MouseSmoothingToggle", "Toggle mouse smoothing (planned).", ModuleCategory.CAMERA));
        register(new ToggleModule("rawmouseinput", "RawMouseInput", "Use raw mouse input (planned).", ModuleCategory.CAMERA));
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
        register(new ToggleModule("nohurtcam", "NoHurtCam", "Disable hurt camera tilt.", ModuleCategory.VISUAL));
        register(new ToggleModule("timechanger", "TimeChanger", "Client-side time override.", ModuleCategory.VISUAL));
        register(new ToggleModule("weatherchanger", "WeatherChanger", "Client-side weather override.", ModuleCategory.VISUAL));
        register(new ToggleModule("weatherdisabler", "WeatherDisabler", "Disable client-side weather.", ModuleCategory.VISUAL));
        register(new ToggleModule("fogremover", "FogRemover", "Reduce fog.", ModuleCategory.VISUAL));
        register(new ToggleModule("clouddisabler", "CloudDisabler", "Disable clouds.", ModuleCategory.VISUAL));
        register(new ToggleModule("shadowdisabler", "ShadowDisabler", "Disable entity shadows.", ModuleCategory.VISUAL));
        register(new ToggleModule("blockoutline", "BlockOutline", "Enhanced block outlines (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("blockhighlight", "BlockHighlight", "Highlight targeted blocks (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("blockoverlay", "BlockOverlay", "Extra block overlay render (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("entityhighlight", "EntityHighlight", "Highlight entities (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("playeresp", "PlayerESP", "Player ESP overlay (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("projectiletrail", "ProjectileTrail", "Render projectile trails (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("arrowesp", "ArrowESP", "Highlight arrows (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("pearltrajectory", "PearlTrajectory", "Predict pearl trajectory (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("throwablepredictor", "ThrowablePredictor", "Predict throwable arcs (planned).", ModuleCategory.VISUAL));
        register(new ToggleModule("healthparticles", "HealthParticles", "Spawn health/damage particles (planned).", ModuleCategory.VISUAL));

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
        register(new ToggleModule("idlefpslock", "IdleFPSLock", "Lock FPS lower while idle.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("smartvsync", "SmartVSync", "Smart VSync controller (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("framepacing", "FramePacing", "Stabilize frame pacing (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("inputdelayreducer", "InputDelayReducer", "Reduce input delay where possible (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("anticrash", "AntiCrash", "Guard against common client crashes (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("packetlimiter", "PacketLimiter", "Throttle risky packet bursts (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("entitylimiter", "EntityLimiter", "Limit heavy entity renders (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("worldunloadoptimizer", "WorldUnloadOptimizer", "Optimize world unload tasks (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("chunkcache", "ChunkCache", "Cache chunks for smoother swaps (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("fastchunkswap", "FastChunkSwap", "Faster chunk swap handling (planned).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("worldpreloader", "WorldPreloader", "Preload nearby world chunks (planned).", ModuleCategory.PERFORMANCE));
        register(new RamCleanerModule());
        register(new ToggleModule("gcoptimizer", "GCOptimizer", "Auto-GC under memory pressure (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("threadoptimizer", "ThreadOptimizer", "Chunk builder tweaks (lite).", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("networkoptimizer", "NetworkOptimizer", "Enable native transport when possible.", ModuleCategory.PERFORMANCE));
        register(new ToggleModule("soundengineoptimizer", "SoundEngineOptimizer", "Disable some audio features (lite).", ModuleCategory.PERFORMANCE));

        // Movement
        register(new InventoryWalkModule());
        register(new ToggleModule("scrollfix", "ScrollFix", "Fix scroll behavior on mobile/trackpads (planned).", ModuleCategory.MOVEMENT));
        register(new ToggleModule("togglesprint", "ToggleSprint", "Enable vanilla toggle sprint option.", ModuleCategory.MOVEMENT));
        register(new AutoSprintModule());
        register(new ToggleModule("togglesneak", "ToggleSneak", "Enable vanilla toggle sneak option.", ModuleCategory.MOVEMENT));
        register(new ToggleModule("quickdrop", "QuickDrop", "Drop full stacks without CTRL.", ModuleCategory.MOVEMENT));

    }
}
