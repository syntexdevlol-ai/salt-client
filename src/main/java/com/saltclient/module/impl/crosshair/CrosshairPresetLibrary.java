package com.saltclient.module.impl.crosshair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CrosshairPresetLibrary {
    public record Preset(String name, int size, int gap, int thickness, boolean dot, CrosshairColor color) {}

    private static final List<Preset> PRESETS = create();

    private CrosshairPresetLibrary() {}

    public static int size() {
        return PRESETS.size();
    }

    public static Preset get(int index) {
        if (PRESETS.isEmpty()) return new Preset("Default", 6, 3, 2, false, CrosshairColor.WHITE);
        int i = Math.floorMod(index, PRESETS.size());
        return PRESETS.get(i);
    }

    public static List<Preset> all() {
        return Collections.unmodifiableList(PRESETS);
    }

    private static List<Preset> create() {
        List<Preset> out = new ArrayList<>();

        int[] sizes = {4, 6, 8, 10, 12};
        int[] gaps = {0, 2, 4, 6};
        int[] thicknesses = {1, 2, 3};
        boolean[] dots = {false, true};

        for (CrosshairColor color : CrosshairColor.values()) {
            for (int size : sizes) {
                for (int gap : gaps) {
                    for (int thickness : thicknesses) {
                        for (boolean dot : dots) {
                            String name = color.label + " S" + size + " G" + gap + " T" + thickness + (dot ? " Dot" : "");
                            out.add(new Preset(name, size, gap, thickness, dot, color));
                        }
                    }
                }
            }
        }

        return out;
    }
}
