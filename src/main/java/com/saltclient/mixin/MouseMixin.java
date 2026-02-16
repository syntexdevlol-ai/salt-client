package com.saltclient.mixin;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.impl.visual.ZoomModule;
import com.saltclient.state.SaltState;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public final class MouseMixin {
    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void salt_onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (!SaltClient.MODULES.isEnabled("zoomscroll")) return;
        if (!SaltState.zooming) return;

        Module m = SaltClient.MODULES.byId("zoom").orElse(null);
        if (!(m instanceof ZoomModule zm)) return;
        if (vertical > 0) zm.adjustZoomFov(-2);
        else if (vertical < 0) zm.adjustZoomFov(2);

        // Prevent hotbar scrolling while zooming.
        ci.cancel();
    }
}
