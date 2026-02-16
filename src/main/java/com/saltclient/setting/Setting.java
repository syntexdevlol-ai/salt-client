package com.saltclient.setting;

import com.google.gson.JsonElement;

/**
 * Small, type-safe setting used by modules and rendered in the settings UI.
 *
 * Kept intentionally simple (no reflection).
 */
public abstract class Setting<T> {
    private final String id;
    private final String name;
    private final String description;
    private final T defaultValue;
    private T value;

    protected Setting(String id, String name, String description, T defaultValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
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

    public final T getDefaultValue() {
        return defaultValue;
    }

    public final T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public void reset() {
        this.value = defaultValue;
    }

    public abstract JsonElement toJson();

    public abstract void fromJson(JsonElement el);
}

