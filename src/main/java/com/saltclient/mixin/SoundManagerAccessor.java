package com.saltclient.mixin;

import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accesses the {@link SoundSystem} instance owned by {@link SoundManager}.
 *
 * <p>We use this to get the internal {@link net.minecraft.client.sound.Channel}
 * so our Song Player can stream audio through Minecraft's OpenAL backend.
 */
@Mixin(SoundManager.class)
public interface SoundManagerAccessor {
    @Accessor("soundSystem")
    SoundSystem saltclient$getSoundSystem();
}

