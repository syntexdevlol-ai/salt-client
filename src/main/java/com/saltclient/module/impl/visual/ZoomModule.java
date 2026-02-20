package com.saltclient.module.impl.visual;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.module.KeybindMode;
import com.saltclient.setting.IntSetting;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

/**
 * Simple zoom:
 * - Enable the module (or bind it to a key with HOLD mode)
 *
 * Implemented by temporarily lowering the client's FOV option.
 */
public final class ZoomModule extends Module {
    private Integer prevFov;
    private final IntSetting zoomFov;

    public ZoomModule() {
        super("zoom", "Zoom", "Hold C to zoom (adjusts FOV).", ModuleCategory.CAMERA, true);
        this.zoomFov = addSetting(new IntSetting("zoomFov", "Zoom FOV", "Lower = more zoom.", 30, 10, 70, 1));
        getBindKeySetting().setValue(GLFW.GLFW_KEY_C);
        getBindModeSetting().setValue(KeybindMode.HOLD);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        startZoom(mc);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;

        // Only touch FOV while an actual zoom is in progress (Hold key or module toggled on).
        if (!SaltState.zooming) return;
        applyZoom(mc);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        stopZoom(mc);
    }

    public int getZoomFov() {
        return zoomFov.getValue();
    }

    public void adjustZoomFov(int delta) {
        zoomFov.setValue(zoomFov.getValue() + delta);
        SaltClient.CONFIG.save(SaltClient.MODULES);
    }

    private void startZoom(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        if (prevFov == null) prevFov = mc.options.getFov().getValue();
        SaltState.zooming = true;
    }

    private void applyZoom(MinecraftClient mc) {
        mc.options.getFov().setValue(zoomFov.getValue());
    }

    private void stopZoom(MinecraftClient mc) {
        if (mc == null || mc.options == null) return;
        SaltState.zooming = false;
        if (prevFov == null) return;
        mc.options.getFov().setValue(prevFov);
        prevFov = null;
    }
}
