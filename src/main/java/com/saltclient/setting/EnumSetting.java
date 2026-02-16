package com.saltclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
    private final E[] values;

    @SafeVarargs
    public EnumSetting(String id, String name, String description, E defaultValue, E... values) {
        super(id, name, description, defaultValue);
        this.values = values;
    }

    public void next() {
        E cur = getValue();
        if (values == null || values.length == 0) return;
        int idx = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == cur) {
                idx = i;
                break;
            }
        }
        setValue(values[(idx + 1) % values.length]);
    }

    @Override
    public JsonElement toJson() {
        E v = getValue();
        return new JsonPrimitive(v == null ? "" : v.name());
    }

    @Override
    public void fromJson(JsonElement el) {
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            try {
                String s = el.getAsString();
                if (s == null || s.isEmpty()) return;
                for (E v : values) {
                    if (v != null && v.name().equalsIgnoreCase(s)) {
                        setValue(v);
                        return;
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}

