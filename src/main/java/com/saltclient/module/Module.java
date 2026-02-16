package com.saltclient.module;

import com.saltclient.SaltClient;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.KeySetting;
import com.saltclient.setting.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    private final List<Setting<?>> settings = new ArrayList<>();
    private final KeySetting bindKey;
    private final EnumSetting<KeybindMode> bindMode;

    private boolean prevBindDown;
    private boolean holdActive;
    private boolean holdPrevEnabled;

    protected Module(String id, String name, String description, ModuleCategory category, boolean implemented) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.implemented = implemented;

        // Built-in keybind settings for every module (optional to use; default unbound).
        this.bindKey = addSetting(new KeySetting("key", "Keybind", "Press to toggle/hold this module. (-1 = none)", -1));
        this.bindMode = addSetting(new EnumSetting<>("keyMode", "Key Mode", "How the keybind behaves.", KeybindMode.TOGGLE, KeybindMode.values()));
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

    public final List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public final Setting<?> getSetting(String id) {
        if (id == null) return null;
        for (Setting<?> s : settings) {
            if (id.equals(s.getId())) return s;
        }
        return null;
    }

    public final boolean hasSettings() {
        return !settings.isEmpty();
    }

    public final KeySetting getBindKeySetting() {
        return bindKey;
    }

    public final EnumSetting<KeybindMode> getBindModeSetting() {
        return bindMode;
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

    /**
     * Used for runtime-only changes (ex: HOLD keybind mode) without saving the config repeatedly.
     */
    public final void setEnabledRuntime(boolean enabled) {
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

    /**
     * Called by ModuleManager to update keybind behavior.
     */
    public final void onBindTick(MinecraftClient mc) {
        if (mc == null || mc.getWindow() == null) return;

        int key = bindKey.getValue();
        if (key < 0) {
            // If the key is unbound, make sure we don't leave hold state stuck.
            prevBindDown = false;
            if (holdActive) {
                holdActive = false;
                if (!holdPrevEnabled) setEnabledRuntime(false);
            }
            return;
        }

        boolean down = net.minecraft.client.util.InputUtil.isKeyPressed(mc.getWindow().getHandle(), key);
        KeybindMode mode = bindMode.getValue();

        if (mode == KeybindMode.HOLD) {
            if (down && !holdActive) {
                holdActive = true;
                holdPrevEnabled = isEnabled();
                if (!holdPrevEnabled) setEnabledRuntime(true);
            } else if (!down && holdActive) {
                holdActive = false;
                if (!holdPrevEnabled) setEnabledRuntime(false);
            }
        } else {
            if (holdActive) {
                // If user switches away from HOLD while holding the key, restore previous state.
                holdActive = false;
                if (!holdPrevEnabled) setEnabledRuntime(false);
            }
            if (down && !prevBindDown) {
                toggle();
            }
        }

        prevBindDown = down;
    }

    protected final <S extends Setting<?>> S addSetting(S setting) {
        settings.add(setting);
        return setting;
    }

    protected void onEnable(MinecraftClient mc) {}

    protected void onDisable(MinecraftClient mc) {}

    public void onTick(MinecraftClient mc) {}

    public void onHudRender(DrawContext ctx) {}
}
