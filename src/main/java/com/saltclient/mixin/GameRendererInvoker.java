package com.saltclient.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Access to the private shader loader used by vanilla post-processing.
 */
@Mixin(GameRenderer.class)
public interface GameRendererInvoker {
    @Invoker("loadPostProcessor")
    void saltclient$loadPostProcessor(Identifier id);
}
