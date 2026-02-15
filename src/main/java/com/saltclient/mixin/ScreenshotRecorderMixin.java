package com.saltclient.mixin;

import com.saltclient.state.SaltState;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

@Mixin(ScreenshotRecorder.class)
public final class ScreenshotRecorderMixin {
    @Inject(method = "saveScreenshot(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private static void salt_saveScreenshot(File gameDirectory, String fileName, Framebuffer framebuffer, Consumer<Text> consumer, CallbackInfo ci) {
        SaltState.lastScreenshotMs = System.currentTimeMillis();
        SaltState.lastScreenshotName = fileName;
    }
}

