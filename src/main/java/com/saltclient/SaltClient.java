package com.saltclient;

import com.saltclient.gui.SaltScreen;
import com.saltclient.module.ModuleManager;
import com.saltclient.audio.SongPlayerService;
import com.saltclient.tweaks.MemoryTweaks;
import com.saltclient.tweaks.OptionTweaks;
import com.saltclient.tweaks.WorldTweaks;
import com.saltclient.util.ActivityTracker;
import com.saltclient.util.CombatTracker;
import com.saltclient.util.ConfigManager;
import com.saltclient.util.InputTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public final class SaltClient implements ClientModInitializer {
    public static final String MOD_ID = "saltclient";

    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final ConfigManager CONFIG = new ConfigManager(MOD_ID);

    public static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        MODULES.registerDefaults();
        CONFIG.load(MODULES);

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.saltclient.open_menu",
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            "category.saltclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Open menu key
            while (openMenuKey.wasPressed()) {
                if (!(client.currentScreen instanceof SaltScreen)) {
                    client.setScreen(new SaltScreen());
                }
            }

            ActivityTracker.tick(client);
            InputTracker.tick(client);
            MODULES.onTick(client);
            SongPlayerService.tick(client);

            OptionTweaks.tick(client);
            WorldTweaks.tick(client);
            MemoryTweaks.tick(client);
            CombatTracker.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> MODULES.onHudRender(drawContext));
    }
}
