package com.saltclient.module.impl.chat;

import com.saltclient.gui.EmoteMenuScreen;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

/**
 * Opens a small "emote menu" screen.
 *
 * This module is trigger-like: when enabled it opens the menu once, then disables itself
 * without persisting the enabled state to config.
 */
public final class EmoteMenuModule extends Module {
    public EmoteMenuModule() {
        super("emotemenu", "EmoteMenu", "Quick emote buttons for chat.", ModuleCategory.CHAT, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        if (mc != null) {
            mc.setScreen(new EmoteMenuScreen(mc.currentScreen));
        }
        // Disable immediately (runtime-only) so it behaves like "open menu" not a persistent toggle.
        setEnabledRuntime(false);
    }
}

