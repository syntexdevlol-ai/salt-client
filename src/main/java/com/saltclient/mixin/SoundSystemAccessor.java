package com.saltclient.mixin;

import net.minecraft.client.sound.Channel;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses internal fields of {@link SoundSystem}.
 */
@Mixin(SoundSystem.class)
public interface SoundSystemAccessor {
    @Accessor("channel")
    Channel saltclient$getChannel();
}

