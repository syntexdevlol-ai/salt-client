package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.IntSetting;

public final class GuiModule extends Module {
    private final BoolSetting guiAnimations = addSetting(new BoolSetting("guiAnimations", "GUI Animations", "Animate Salt GUI panels and toggles.", true));
    private final IntSetting animationSpeedMs = addSetting(new IntSetting("animationSpeed", "Animation Speed", "GUI open animation duration in milliseconds.", 220, 80, 600, 10));
    private final BoolSetting customMainMenu = addSetting(new BoolSetting("customMainMenu", "Custom Main Menu", "Enable Salt styling on the Minecraft title screen.", true));
    private final BoolSetting customPanorama = addSetting(new BoolSetting("customPanorama", "Custom Panorama", "Use image files from the saltclient/panoramas folder as menu background.", true));

    public GuiModule() {
        super("guimodule", "GUI", "Configure Salt GUI animations, menu style, and panorama behavior.", ModuleCategory.MISC, true);
        setEnabledFromConfig(true);
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

    public boolean customPanoramaEnabled() {
        return customPanorama.getValue();
    }
}
