package com.saltclient.module;

import com.saltclient.SaltClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Base module type.
 *
 * Modules can optionally:
 * - do work every tick (onTick)
 * - render on the HUD (onHudRender)
 */
public abstract class Module {
    private final String id;
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final boolean implemented;

    private boolean enabled;

    protected Module(String id, String name, String description, ModuleCategory category, boolean implemented) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.implemented = implemented;
    }

    public final String getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final ModuleCategory getCategory() {
        return category;
    }

    public final boolean isImplemented() {
        return implemented;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        setEnabled(enabled, true);
    }

    /**
     * Used by the config loader to apply saved states without writing the config back repeatedly.
     */
    public final void setEnabledFromConfig(boolean enabled) {
        setEnabled(enabled, false);
    }

    private void setEnabled(boolean enabled, boolean persist) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;

        MinecraftClient mc = SaltClient.MC;
        if (enabled) onEnable(mc);
        else onDisable(mc);

        if (persist) {
            // Persist immediately to keep things simple and reliable.
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }
    }

    protected void onEnable(MinecraftClient mc) {}

    protected void onDisable(MinecraftClient mc) {}

    public void onTick(MinecraftClient mc) {}

    public void onHudRender(DrawContext ctx) {}
}
