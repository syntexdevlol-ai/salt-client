package com.saltclient.module.impl.movement;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

public final class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super("autosprint", "AutoSprint", "Automatically sprint while moving forward.", ModuleCategory.MOVEMENT, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;
        if (!mc.options.forwardKey.isPressed()) return;

        mc.player.setSprinting(true);
    }
}

