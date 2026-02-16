package com.saltclient.util;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.setting.IntSetting;

public final class HudScale {
    private HudScale() {}

    public static int percent() {
        Module module = SaltClient.MODULES.byId("hudscale").orElse(null);
        if (module != null) {
            if (module.getSetting("scale") instanceof IntSetting is) {
                return is.getValue();
            }
        }
        return 100;
    }

    public static float factor() {
        int p = percent();
        if (p <= 0) return 1.0f;
        return p / 100.0f;
    }
}
