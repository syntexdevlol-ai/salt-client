package com.saltclient.mixin;

import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public final class EntityMixin {
    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void salt_changeLookDirection(double cursorDeltaX, double cursorDeltaY, CallbackInfo ci) {
        if (!SaltState.freeLookActive) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if ((Object) this != mc.player) return;

        float pitchDelta = (float) cursorDeltaY * 0.15f;
        float yawDelta = (float) cursorDeltaX * 0.15f;

        SaltState.freeLookPitch = MathHelper.clamp(SaltState.freeLookPitch + pitchDelta, -90.0f, 90.0f);
        SaltState.freeLookYaw += yawDelta;
        ci.cancel();
    }
}

