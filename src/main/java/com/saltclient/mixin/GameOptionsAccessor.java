package com.saltclient.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Access to a few GameOptions fields we want to override while a module is enabled.
 *
 * <p>We only use accessors here (no reflection) to keep it stable and easy to audit.
 */
@Mixin(GameOptions.class)
public interface GameOptionsAccessor {
    @Accessor("chatOpacity")
    SimpleOption<Double> saltclient$getChatOpacity();
}

