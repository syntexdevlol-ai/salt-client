package com.saltclient.module.impl.crosshair;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class CustomCrosshairModule extends Module {
    private final IntSetting preset;
    private final IntSetting size;
    private final IntSetting gap;
    private final IntSetting thickness;
    private final BoolSetting dot;
    private final EnumSetting<CrosshairColor> color;

    private int lastPreset = -1;

    public CustomCrosshairModule() {
        super("customcrosshair", "CustomCrosshair", "Render a configurable custom crosshair with preset library.", ModuleCategory.CROSSHAIR, true);
        this.preset = addSetting(new IntSetting("preset", "Preset", "Crosshair preset index (0-599).", 0, 0, Math.max(0, CrosshairPresetLibrary.size() - 1), 1));
        this.size = addSetting(new IntSetting("size", "Size", "Crosshair line size.", 6, 2, 20, 1));
        this.gap = addSetting(new IntSetting("gap", "Gap", "Gap between lines.", 3, 0, 15, 1));
        this.thickness = addSetting(new IntSetting("thickness", "Thickness", "Line thickness.", 2, 1, 6, 1));
        this.dot = addSetting(new BoolSetting("dot", "Dot", "Center dot.", false));
        this.color = addSetting(new EnumSetting<>("color", "Color", "Crosshair color preset.", CrosshairColor.WHITE, CrosshairColor.values()));

        applyPreset(this.preset.getValue());
    }

    public int presetCount() {
        return CrosshairPresetLibrary.size();
    }

    public String presetName() {
        return CrosshairPresetLibrary.get(preset.getValue()).name();
    }

    public void nextPreset() {
        int count = presetCount();
        if (count <= 0) return;
        int next = (preset.getValue() + 1) % count;
        applyPreset(next);
    }

    public void prevPreset() {
        int count = presetCount();
        if (count <= 0) return;
        int next = Math.floorMod(preset.getValue() - 1, count);
        applyPreset(next);
    }

    public void applyPreset(int index) {
        CrosshairPresetLibrary.Preset p = CrosshairPresetLibrary.get(index);
        this.preset.setValue(Math.floorMod(index, Math.max(1, presetCount())));
        this.size.setValue(p.size());
        this.gap.setValue(p.gap());
        this.thickness.setValue(p.thickness());
        this.dot.setValue(p.dot());
        this.color.setValue(p.color());
        this.lastPreset = this.preset.getValue();
    }

    @Override
    public void onTick(MinecraftClient mc) {
        int current = preset.getValue();
        if (current != lastPreset) {
            applyPreset(current);
        }
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
