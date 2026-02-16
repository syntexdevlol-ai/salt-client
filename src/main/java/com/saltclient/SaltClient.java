package com.saltclient;

import com.saltclient.gui.SaltScreen;
import com.saltclient.module.ModuleManager;
import com.saltclient.tweaks.MemoryTweaks;
import com.saltclient.tweaks.OptionTweaks;
import com.saltclient.tweaks.WorldTweaks;
import com.saltclient.util.ActivityTracker;
import com.saltclient.util.CombatTracker;
import com.saltclient.util.ConfigManager;
import com.saltclient.util.GuiSettings;
import com.saltclient.util.InputTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

public final class SaltClient implements ClientModInitializer {
    public static final String MOD_ID = "saltclient";

    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final ModuleManager MODULES = new ModuleManager();
    public static final ConfigManager CONFIG = new ConfigManager(MOD_ID);

    private static boolean menuKeyDown;

    @Override
    public void onInitializeClient() {
        MODULES.registerDefaults();
        CONFIG.load(MODULES);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickMenuKey(client);

            ActivityTracker.tick(client);
            InputTracker.tick(client);
            MODULES.onTick(client);

            OptionTweaks.tick(client);
            WorldTweaks.tick(client);
            MemoryTweaks.tick(client);
            CombatTracker.tick(client);
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> MODULES.onHudRender(drawContext));
    }

    private static void tickMenuKey(MinecraftClient client) {
        if (client == null || client.getWindow() == null) return;

        int menuKey = GuiSettings.menuKey();
        if (menuKey < 0) {
            menuKeyDown = false;
            return;
        }

        boolean down = InputUtil.isKeyPressed(client.getWindow().getHandle(), menuKey);
        if (down && !menuKeyDown) {
            if (!(client.currentScreen instanceof SaltScreen)) {
                client.setScreen(new SaltScreen());
            }
        }
        menuKeyDown = down;
    }
}
