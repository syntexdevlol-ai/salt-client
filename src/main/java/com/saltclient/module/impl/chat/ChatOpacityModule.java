package com.saltclient.module.impl.chat;

import com.saltclient.mixin.GameOptionsAccessor;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Overrides vanilla chat opacity while enabled.
 *
 * This is intentionally simple:
 * - saves the previous value on enable
 * - applies the configured value (0..100%) while enabled
 * - restores previous value on disable
 */
public final class ChatOpacityModule extends Module {
    private final IntSetting opacity = addSetting(new IntSetting(
        "opacity",
        "Opacity",
        "Chat opacity percent (0-100).",
        100,
        0,
        100,
        5
    ));

    private Double prevOpacity;

    public ChatOpacityModule() {
        super("chatopacity", "ChatOpacity", "Override chat opacity while enabled.", ModuleCategory.CHAT, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        apply(mc, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        apply(mc, false);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        restore(mc);
    }

    private void apply(MinecraftClient mc, boolean capturePrev) {
        if (mc == null || mc.options == null) return;

        SimpleOption<Double> opt = ((GameOptionsAccessor) mc.options).saltclient$getChatOpacity();
        if (opt == null) return;

        if (capturePrev) {
            prevOpacity = opt.getValue();
        }

        double v = opacity.getValue() / 100.0;
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;

        if (!opt.getValue().equals(v)) {
            opt.setValue(v);
        }
    }

    private void restore(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        if (prevOpacity == null) return;

        SimpleOption<Double> opt = ((GameOptionsAccessor) mc.options).saltclient$getChatOpacity();
        if (opt == null) return;

        opt.setValue(prevOpacity);
        prevOpacity = null;
    }
}

