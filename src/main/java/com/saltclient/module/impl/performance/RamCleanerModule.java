package com.saltclient.module.impl.performance;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.tweaks.MemoryTweaks;
import net.minecraft.client.MinecraftClient;

/**
 * One-shot action: requests a GC and disables itself.
 */
public final class RamCleanerModule extends Module {
    public RamCleanerModule() {
        super("ramcleaner", "RAMCleaner", "Request a GC (one-shot).", ModuleCategory.PERFORMANCE, true);
    }

    @Override
    protected void onEnable(MinecraftClient mc) {
        MemoryTweaks.runCleaner(mc);
        // Disable again so it behaves like a button.
        setEnabledFromConfig(false);
        SaltClient.CONFIG.save(SaltClient.MODULES);
    }
}
