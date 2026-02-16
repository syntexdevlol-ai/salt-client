package com.saltclient.module.impl.combat;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class HitColorModule extends Module {
    private final EnumSetting<HitMarkerColor> color;
    private final IntSetting size;
    private final IntSetting gap;
    private final IntSetting thickness;
    private final IntSetting durationMs;

    public HitColorModule() {
        super("hitcolor", "HitColor", "Shows a colored hit marker on attack (client-side).", ModuleCategory.COMBAT, true);
        this.color = addSetting(new EnumSetting<>("color", "Color", "Hit marker color.", HitMarkerColor.RED, HitMarkerColor.values()));
        this.size = addSetting(new IntSetting("size", "Size", "Marker arm length.", 6, 2, 14, 1));
        this.gap = addSetting(new IntSetting("gap", "Gap", "Distance from crosshair center.", 6, 1, 18, 1));
        this.thickness = addSetting(new IntSetting("thickness", "Thickness", "Marker thickness.", 2, 1, 6, 1));
        this.durationMs = addSetting(new IntSetting("durationMs", "Duration", "Visible time in milliseconds.", 150, 60, 400, 10));
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (SaltState.lastHitMs == 0L || (now - SaltState.lastHitMs) > durationMs.getValue()) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        int c = color.getValue().argb;
        int s = size.getValue();
        int g = gap.getValue();
        int t = thickness.getValue();

        // Simple "X" marker
        ctx.fill(cx - g - s, cy - g - s, cx - g - s + t, cy - g - s + t, c);
        ctx.fill(cx + g + s - t, cy - g - s, cx + g + s, cy - g - s + t, c);
        ctx.fill(cx - g - s, cy + g + s - t, cx - g - s + t, cy + g + s, c);
        ctx.fill(cx + g + s - t, cy + g + s - t, cx + g + s, cy + g + s, c);
    }
}
