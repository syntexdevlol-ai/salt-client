package com.saltclient.module.impl.hud;

import com.saltclient.SaltClient;
import com.saltclient.gui.HudEditorScreen;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * One-shot: opens the HUD editor UI and disables itself.
 */
public final class HudEditorModule extends Module {
    public HudEditorModule() {
        super("hudeditor", "HUDEditor", "Drag HUD elements.", ModuleCategory.HUD, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        Screen parent = MinecraftClient.getInstance().currentScreen;
        MinecraftClient.getInstance().setScreen(new HudEditorScreen(parent));
        // Disable again so it behaves like a button.
        setEnabledFromConfig(false);
        SaltClient.CONFIG.save(SaltClient.MODULES);
    }
}

