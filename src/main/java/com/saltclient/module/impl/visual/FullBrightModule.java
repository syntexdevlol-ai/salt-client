package com.saltclient.module.impl.visual;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

public final class FullBrightModule extends Module {
    private Double prevGamma;

    public FullBrightModule() {
        super("fullbright", "FullBright", "Boost gamma for a fullbright effect.", ModuleCategory.VISUAL, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        if (mc.options == null) return;
        SimpleOption<Double> gamma = mc.options.getGamma();
        prevGamma = gamma.getValue();
        gamma.setValue(16.0);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        if (mc.options == null || prevGamma == null) return;
        mc.options.getGamma().setValue(prevGamma);
        prevGamma = null;
    }
}

