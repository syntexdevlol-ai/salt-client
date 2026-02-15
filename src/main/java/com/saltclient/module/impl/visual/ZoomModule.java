package com.saltclient.module.impl.visual;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Simple zoom:
 * - Enable the module
 * - Hold the Zoom key (default: C)
 *
 * Implemented by temporarily lowering the client's FOV option.
 */
public final class ZoomModule extends Module {
    private Integer prevFov;

    public ZoomModule() {
        super("zoom", "Zoom", "Hold C to zoom (adjusts FOV).", ModuleCategory.CAMERA, true);
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.options == null || SaltClient.zoomKey == null) return;

        boolean shouldZoom = SaltClient.zoomKey.isPressed();
        if (shouldZoom && !SaltState.zooming) startZoom(mc);
        if (SaltState.zooming && shouldZoom) applyZoom(mc);
        if (!shouldZoom && SaltState.zooming) stopZoom(mc);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        // Safety: if the module gets disabled while zooming, restore the old value.
        stopZoom(mc);
    }

    private void startZoom(MinecraftClient mc) {
        prevFov = mc.options.getFov().getValue();
        SaltState.zooming = true;
        SaltState.clampZoom();
    }

    private void applyZoom(MinecraftClient mc) {
        SaltState.clampZoom();
        mc.options.getFov().setValue(SaltState.zoomFov);
    }

    private void stopZoom(MinecraftClient mc) {
        if (!SaltState.zooming) return;
        SaltState.zooming = false;
        if (mc.options == null || prevFov == null) return;
        mc.options.getFov().setValue(prevFov);
        prevFov = null;
    }
}
