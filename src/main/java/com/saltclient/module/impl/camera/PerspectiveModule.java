package com.saltclient.module.impl.camera;

import com.saltclient.module.KeybindMode;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.lwjgl.glfw.GLFW;

/**
 * Simple perspective module:
 * - Default: HOLD V
 * - When enabled, forces third person; when disabled, restores previous.
 */
public final class PerspectiveModule extends Module {
    private Perspective prev;

    public PerspectiveModule() {
        super("perspective", "Perspective", "Force third person while enabled.", ModuleCategory.CAMERA, true);
        getBindKeySetting().setValue(GLFW.GLFW_KEY_V);
        getBindModeSetting().setValue(KeybindMode.HOLD);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        prev = mc.options.getPerspective();
        mc.options.setPerspective(Perspective.THIRD_PERSON_BACK);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        if (prev != null) mc.options.setPerspective(prev);
        prev = null;
    }
}

