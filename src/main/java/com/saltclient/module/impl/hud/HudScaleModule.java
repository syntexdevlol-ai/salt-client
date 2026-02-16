package com.saltclient.module.impl.hud;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.IntSetting;

public final class HudScaleModule extends Module {
    private final IntSetting scale = addSetting(new IntSetting("scale", "HUD Scale", "Scale Salt HUD elements (%).", 100, 70, 160, 5));

    public HudScaleModule() {
        super("hudscale", "HUDScale", "Change size of Salt HUD elements.", ModuleCategory.HUD, true);
        setEnabledFromConfig(true);
    }

    public int percent() {
        return scale.getValue();
    }
}
