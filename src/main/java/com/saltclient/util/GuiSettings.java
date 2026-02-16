package com.saltclient.util;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.impl.misc.GuiModule;
import org.lwjgl.glfw.GLFW;

public final class GuiSettings {
    private GuiSettings() {}

    public static int menuKey() {
        GuiModule module = guiModule();
        return module == null ? GLFW.GLFW_KEY_RIGHT_SHIFT : module.menuKey();
    }

    public static boolean animationsEnabled() {
        GuiModule module = guiModule();
        return module == null || module.animationsEnabled();
    }

    public static int animationSpeedMs() {
        GuiModule module = guiModule();
        return module == null ? 220 : module.animationSpeedMs();
    }

    public static boolean customMainMenuEnabled() {
        GuiModule module = guiModule();
        return module == null || module.customMainMenuEnabled();
    }

    private static GuiModule guiModule() {
        Module module = SaltClient.MODULES.byId("guimodule").orElse(null);
        if (module instanceof GuiModule guiModule) {
            return guiModule;
        }
        return null;
    }
}
