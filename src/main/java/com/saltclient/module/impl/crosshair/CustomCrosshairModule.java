package com.saltclient.module.impl.crosshair;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class CustomCrosshairModule extends Module {
    private final IntSetting size;
    private final IntSetting gap;
    private final IntSetting thickness;
    private final BoolSetting dot;
    private final EnumSetting<CrosshairColor> color;

    public CustomCrosshairModule() {
        super("customcrosshair", "CustomCrosshair", "Render a configurable custom crosshair.", ModuleCategory.CROSSHAIR, true);
        this.size = addSetting(new IntSetting("size", "Size", "Crosshair line size.", 6, 2, 20, 1));
        this.gap = addSetting(new IntSetting("gap", "Gap", "Gap between lines.", 3, 0, 15, 1));
        this.thickness = addSetting(new IntSetting("thickness", "Thickness", "Line thickness.", 2, 1, 6, 1));
        this.dot = addSetting(new BoolSetting("dot", "Dot", "Center dot.", false));
        this.color = addSetting(new EnumSetting<>("color", "Color", "Crosshair color preset.", CrosshairColor.WHITE, CrosshairColor.values()));
    }

    @Override
    public void onHudRender(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        int size = this.size.getValue();
        int gap = this.gap.getValue();
        int t = this.thickness.getValue();
        int c = this.color.getValue().argb;

        // Horizontal
        ctx.fill(cx - gap - size, cy - t / 2, cx - gap, cy + (t + 1) / 2, c);
        ctx.fill(cx + gap, cy - t / 2, cx + gap + size, cy + (t + 1) / 2, c);
        // Vertical
        ctx.fill(cx - t / 2, cy - gap - size, cx + (t + 1) / 2, cy - gap, c);
        ctx.fill(cx - t / 2, cy + gap, cx + (t + 1) / 2, cy + gap + size, c);

        if (dot.getValue()) {
            ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
        }
    }
}
