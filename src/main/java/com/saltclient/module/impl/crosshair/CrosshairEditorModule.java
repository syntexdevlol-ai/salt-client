package com.saltclient.module.impl.crosshair;

import com.saltclient.SaltClient;
import com.saltclient.gui.CrosshairEditorScreen;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Opens the crosshair editor screen (one-shot).
 */
public final class CrosshairEditorModule extends Module {
    public CrosshairEditorModule() {
        super("crosshaireditor", "CrosshairEditor", "Edit custom crosshair settings.", ModuleCategory.CROSSHAIR, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        Screen parent = MinecraftClient.getInstance().currentScreen;
        MinecraftClient.getInstance().setScreen(new CrosshairEditorScreen(parent));
        // Disable again so it behaves like a button.
        setEnabledFromConfig(false);
        SaltClient.CONFIG.save(SaltClient.MODULES);
    }
}
