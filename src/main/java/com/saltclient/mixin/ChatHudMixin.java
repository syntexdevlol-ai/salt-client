package com.saltclient.mixin;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Mixin(ChatHud.class)
public final class ChatHudMixin {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm");

    private static String lastMessage;
    private static long lastMessageMs;
    private static long lastAutoGgMs;

    @Inject(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void salt_beforeAddMessage(Text message, MessageSignatureData signature, MessageIndicator indicator, CallbackInfo ci) {
        String raw = message.getString();
        long now = System.currentTimeMillis();

        if (SaltClient.MODULES.isEnabled("chatcleaner")) {
            if (raw.equals(lastMessage) && (now - lastMessageMs) < 1500L) {
                ci.cancel();
                return;
            }
            lastMessage = raw;
            lastMessageMs = now;
        }

        if (SaltClient.MODULES.isEnabled("chatautogg") && (now - lastAutoGgMs) > 60_000L) {
            if (shouldAutoGg(raw)) {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc.player != null && mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendChatMessage("gg");
                    lastAutoGgMs = now;
                }
            }
        }
    }

    @ModifyVariable(
        method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private Text salt_timestamp(Text message) {
        if (!SaltClient.MODULES.isEnabled("chattimestamp")) return message;
        String ts = TS.format(LocalTime.now());
        return Text.literal("[" + ts + "] ").formatted(Formatting.DARK_GRAY).append(message);
    }

    private static boolean shouldAutoGg(String raw) {
        if (raw == null) return false;
        String s = raw.toLowerCase(Locale.ROOT);
        return s.contains("victory")
            || s.contains("you won")
            || s.contains("game over")
            || s.contains("won the game")
            || s.contains("match over")
            || s.contains("winner");
    }
}

