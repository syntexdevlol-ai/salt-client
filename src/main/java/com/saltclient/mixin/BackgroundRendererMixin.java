package com.saltclient.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.saltclient.SaltClient;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.FogShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public final class BackgroundRendererMixin {
    @Inject(method = "applyFog", at = @At("RETURN"))
    private static void salt_applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
        // Don't cancel vanilla fog setup; just override the fog distance afterward.
        // Cancelling can cause visual glitches in some situations (dimension-specific fog rules, etc).

        if (SaltClient.MODULES.isEnabled("fogremover")) {
            RenderSystem.setShaderFogStart(0.0f);
            RenderSystem.setShaderFogEnd(viewDistance * 2.0f);
            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
            return;
        }

        if (SaltClient.MODULES.isEnabled("clearwater") && camera.getSubmersionType() == CameraSubmersionType.WATER) {
            RenderSystem.setShaderFogStart(0.0f);
            RenderSystem.setShaderFogEnd(viewDistance);
            RenderSystem.setShaderFogShape(FogShape.CYLINDER);
        }
    }
}
