package com.saltclient.module.impl.performance;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

/**
 * Toggles the built-in "Force Unicode Font" option.
 *
 * This is a visible change (especially for HUD text), and is also useful on some devices
 * that struggle with certain glyphs.
 */
public final class FontRendererModule extends Module {
    public FontRendererModule() {
        super("fontrenderer", "FontRenderer", "Toggle Force Unicode Font.", ModuleCategory.PERFORMANCE, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        if (mc.options == null) return;
        mc.options.getForceUnicodeFont().setValue(true);
        mc.reloadResources();
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        // OptionTweaks will restore the baseline value.
        mc.reloadResources();
    }
}

