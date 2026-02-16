package com.saltclient.module.impl.combat;

public enum HitMarkerColor {
    RED("Red", 0xFFFF5C5C),
    WHITE("White", 0xFFFFFFFF),
    GREEN("Green", 0xFF7BC96F),
    CYAN("Cyan", 0xFF8BE0FF),
    YELLOW("Yellow", 0xFFFFD66E),
    PINK("Pink", 0xFFFF84C8);

    public final String label;
    public final int argb;

    HitMarkerColor(String label, int argb) {
        this.label = label;
        this.argb = argb;
    }

    @Override
    public String toString() {
        return label;
    }
}
