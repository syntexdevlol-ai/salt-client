package com.saltclient.mixin;

import com.saltclient.SaltClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public final class InGameHudMixin {
    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void salt_renderCrosshair(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("customcrosshair")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void salt_renderStatusEffectOverlay(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("minimalhud")) {
            ci.cancel();
        }
    }

    // In 1.21.1 there are multiple overloads of renderScoreboardSidebar; target them explicitly.
    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void salt_renderScoreboardSidebar(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("minimalhud") || SaltClient.MODULES.isEnabled("noscoreboard")) {
            ci.cancel();
        }
    }

    @Inject(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void salt_renderScoreboardSidebarObjective(DrawContext ctx, ScoreboardObjective objective, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("minimalhud") || SaltClient.MODULES.isEnabled("noscoreboard")) {
            ci.cancel();
        }
    }

    @Inject(method = "renderChat", at = @At("HEAD"), cancellable = true)
    private void salt_renderChat(DrawContext ctx, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (SaltClient.MODULES.isEnabled("minimalhud")) {
            ci.cancel();
        }
    }
}
