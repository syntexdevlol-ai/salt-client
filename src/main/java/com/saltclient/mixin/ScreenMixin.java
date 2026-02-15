package com.saltclient.mixin;

import com.saltclient.SaltClient;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Inject(method = "blur", at = @At("HEAD"), cancellable = true)
    private void salt_blur(CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("uiblurtoggle")) {
            ci.cancel();
        }
    }
}

