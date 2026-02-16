package com.saltclient.module.impl.crosshair;

public enum CrosshairColor {
    WHITE("White", 0xFFE6ECFF),
    GREEN("Green", 0xFF7BC96F),
    RED("Red", 0xFFFF5C5C),
    CYAN("Cyan", 0xFF8BE0FF),
    YELLOW("Yellow", 0xFFFFD66E);

    public final String label;
    public final int argb;

    CrosshairColor(String label, int argb) {
        this.label = label;
        this.argb = argb;
    }

    @Override
    public String toString() {
        return label;
    }
}

