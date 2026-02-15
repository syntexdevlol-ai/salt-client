package com.saltclient;

import com.saltclient.gui.SaltScreen;
import com.saltclient.module.ModuleManager;
import com.saltclient.util.ConfigManager;
import com.saltclient.state.SaltState;
import com.saltclient.tweaks.MemoryTweaks;
import com.saltclient.tweaks.OptionTweaks;
import com.saltclient.tweaks.WorldTweaks;
import com.saltclient.util.ActivityTracker;
import com.saltclient.util.CombatTracker;
import com.saltclient.util.InputTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/**
 * Salt Client entrypoint.
 *
 * This mod is intentionally kept simple:
 * - A tiny module system
 * - A basic click-to-toggle menu screen
 * - A few HUD modules implemented (more can be added later)
 */
public final class SaltClient implements ClientModInitializer {
    public static final String MOD_ID = "saltclient";

    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final ConfigManager CONFIG = new ConfigManager(MOD_ID);

    private static KeyBinding openMenuKey;
    public static KeyBinding zoomKey;
    public static KeyBinding perspectiveKey;
    public static KeyBinding freeLookKey;

    private static boolean perspectiveHeld;
    private static Perspective previousPerspective;

    @Override
    public void onInitializeClient() {
        MODULES.registerDefaults();
        CONFIG.load(MODULES);

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.saltclient.open_menu",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.saltclient"
        ));

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.saltclient.zoom",
            GLFW.GLFW_KEY_C,
            "category.saltclient"
        ));

        perspectiveKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.saltclient.perspective",
            GLFW.GLFW_KEY_V,
            "category.saltclient"
        ));

        freeLookKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.saltclient.freelook",
            GLFW.GLFW_KEY_B,
            "category.saltclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open menu key
            while (openMenuKey.wasPressed()) {
                if (!(client.currentScreen instanceof SaltScreen)) client.setScreen(new SaltScreen());
            }

            ActivityTracker.tick(client);
            InputTracker.tick(client);
            MODULES.onTick(client);

            handlePerspective(client);
            handleFreeLook(client);

            // Global tweaks/controllers.
            OptionTweaks.tick(client);
            WorldTweaks.tick(client);
            MemoryTweaks.tick(client);
            CombatTracker.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> MODULES.onHudRender(drawContext));
    }

    private static void handlePerspective(MinecraftClient mc) {
        if (mc.options == null) return;

        boolean enabled = MODULES.isEnabled("perspective");
        boolean held = enabled && perspectiveKey != null && perspectiveKey.isPressed();

        if (held && !perspectiveHeld) {
            perspectiveHeld = true;
            previousPerspective = mc.options.getPerspective();
            mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
        } else if (!held && perspectiveHeld) {
            perspectiveHeld = false;
            if (previousPerspective != null) mc.options.setPerspective(previousPerspective);
            previousPerspective = null;
        }

        if (!enabled && perspectiveHeld) {
            perspectiveHeld = false;
            if (previousPerspective != null) mc.options.setPerspective(previousPerspective);
            previousPerspective = null;
        }
    }

    private static void handleFreeLook(MinecraftClient mc) {
        boolean enabled = MODULES.isEnabled("freelook");
        boolean held = enabled && freeLookKey != null && freeLookKey.isPressed() && mc.player != null;

        if (held && !SaltState.freeLookActive) {
            SaltState.freeLookActive = true;
            SaltState.freeLookYaw = mc.player.getYaw();
            SaltState.freeLookPitch = mc.player.getPitch();
        } else if (!held && SaltState.freeLookActive) {
            SaltState.freeLookActive = false;
        }
    }
}
