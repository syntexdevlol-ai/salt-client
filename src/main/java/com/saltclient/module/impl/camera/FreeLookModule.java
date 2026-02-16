package com.saltclient.module.impl.camera;

import com.saltclient.module.KeybindMode;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * FreeLook:
 * - Default: HOLD B
 * - Locks player rotation while letting the camera rotate.
 */
public final class FreeLookModule extends Module {
    public FreeLookModule() {
        super("freelook", "FreeLook", "Hold to look around without turning your player.", ModuleCategory.CAMERA, true);
        getBindKeySetting().setValue(GLFW.GLFW_KEY_B);
        getBindModeSetting().setValue(KeybindMode.HOLD);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        if (mc == null || mc.player == null) return;
        SaltState.freeLookActive = true;
        SaltState.freeLookYaw = mc.player.getYaw();
        SaltState.freeLookPitch = mc.player.getPitch();
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        SaltState.freeLookActive = false;
    }
}

