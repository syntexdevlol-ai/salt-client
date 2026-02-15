package com.saltclient.mixin;

import com.saltclient.util.ServerTpsTracker;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public final class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onWorldTimeUpdate", at = @At("TAIL"))
    private void salt_onWorldTimeUpdate(WorldTimeUpdateS2CPacket packet, CallbackInfo ci) {
        ServerTpsTracker.onWorldTimeUpdate(packet.getTime());
    }
}

