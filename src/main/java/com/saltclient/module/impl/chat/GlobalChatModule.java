package com.saltclient.module.impl.chat;

import com.saltclient.gui.GlobalChatScreen;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

/**
 * Opens the global chat UI and toggles itself off.
 */
public final class GlobalChatModule extends Module {
    public GlobalChatModule() {
        super("globalchat", "GlobalChat", "Chat with other Salt users (WebSocket).", ModuleCategory.CHAT, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        if (mc != null) {
            mc.setScreen(new GlobalChatScreen());
        }
        setEnabledFromConfig(false);
    }
}
