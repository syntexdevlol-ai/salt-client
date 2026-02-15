package com.saltclient.mixin;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {
    @ModifyArg(
        method = "handleInputEvents",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;dropSelectedItem(Z)Z"),
        index = 0
    )
    private boolean salt_quickDrop(boolean wholeStack) {
        return wholeStack || SaltClient.MODULES.isEnabled("quickdrop");
    }
}

