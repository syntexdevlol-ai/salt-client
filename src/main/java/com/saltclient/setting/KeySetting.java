package com.saltclient.setting;

/**
 * Keybind setting.
 *
 * Value is a GLFW key code. Use -1 for "unbound".
 */
public final class KeySetting extends IntSetting {
    public KeySetting(String id, String name, String description, int defaultValue) {
        // GLFW key codes are typically < 350, but keep a generous max.
        super(id, name, description, defaultValue, -1, 1000, 1);
    }
}

