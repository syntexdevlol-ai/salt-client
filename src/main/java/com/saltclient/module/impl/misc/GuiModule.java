package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.setting.KeySetting;
import org.lwjgl.glfw.GLFW;

public final class GuiModule extends Module {
    private final KeySetting menuKey = addSetting(new KeySetting("menuKey", "Menu Key", "Key used to open the Salt menu.", GLFW.GLFW_KEY_RIGHT_SHIFT));
    private final BoolSetting guiAnimations = addSetting(new BoolSetting("guiAnimations", "GUI Animations", "Animate Salt GUI panels and toggles.", true));
    private final IntSetting animationSpeedMs = addSetting(new IntSetting("animationSpeed", "Animation Speed", "GUI open animation duration in milliseconds.", 220, 80, 600, 10));
    private final BoolSetting customMainMenu = addSetting(new BoolSetting("customMainMenu", "Custom Main Menu", "Enable Salt styling on the Minecraft title screen.", true));

    public GuiModule() {
        super("guimodule", "GUI", "Configure Salt menu keybind, animations, and main menu style.", ModuleCategory.MISC, true);
        setEnabledFromConfig(true);
    }

    public int menuKey() {
        return menuKey.getValue();
    }

    public boolean animationsEnabled() {
        return guiAnimations.getValue();
    }

    public int animationSpeedMs() {
        return animationSpeedMs.getValue();
    }

    public boolean customMainMenuEnabled() {
        return customMainMenu.getValue();
    }
}
