package com.saltclient.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class IntSetting extends Setting<Integer> {
    private final int min;
    private final int max;
    private final int step;

    public IntSetting(String id, String name, String description, int defaultValue, int min, int max, int step) {
        super(id, name, description, defaultValue);
        this.min = min;
        this.max = max;
        this.step = Math.max(1, step);
        setValue(clamp(defaultValue));
    }

    public final int getMin() {
        return min;
    }

    public final int getMax() {
        return max;
    }

    public final int getStep() {
        return step;
    }

    public void inc() {
        setValue(clamp(getValue() + step));
    }

    public void dec() {
        setValue(clamp(getValue() - step));
    }

    @Override
    public void setValue(Integer value) {
        super.setValue(clamp(value == null ? getDefaultValue() : value));
    }

    private int clamp(int v) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(getValue());
    }

    @Override
    public void fromJson(JsonElement el) {
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
            try {
                setValue(el.getAsInt());
            } catch (Exception ignored) {}
        }
    }
}

