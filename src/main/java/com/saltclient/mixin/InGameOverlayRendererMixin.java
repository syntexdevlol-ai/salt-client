package com.saltclient.mixin;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameOverlayRenderer.class)
public final class InGameOverlayRendererMixin {
    @Inject(method = "renderFireOverlay", at = @At("HEAD"), cancellable = true)
    private static void salt_renderFireOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("lowfire")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderUnderwaterOverlay", at = @At("HEAD"), cancellable = true)
    private static void salt_renderUnderwaterOverlay(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("clearwater")) {
            ci.cancel();
        }
    }
}

