package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.impl.crosshair.CustomCrosshairModule;
import com.saltclient.util.UiFonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class CrosshairEditorScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int TEXT = 0xFFE6EBFA;
    private static final int MUTED = 0xFF8EA1C8;

    private final Screen parent;
    private CustomCrosshairModule crosshair;

    public CrosshairEditorScreen() {
        this(null);
    }

    public CrosshairEditorScreen(Screen parent) {
        super(UiFonts.text("Crosshair Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla background blur.
    }

    @Override
    public void close() {
        if (this.client != null && parent != null) {
            this.client.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    protected void init() {
        Module m = SaltClient.MODULES.byId("customcrosshair").orElse(null);
        if (m instanceof CustomCrosshairModule ccm) {
            crosshair = ccm;
        }

        int cx = this.width / 2;
        int y = this.height / 2 + 52;

        int w = 90;
        int h = 18;
        int g = 6;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Preset <"), b -> {
            if (crosshair == null) return;
            crosshair.prevPreset();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Preset >"), b -> {
            if (crosshair == null) return;
            crosshair.nextPreset();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Size -"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.IntSetting) crosshair.getSetting("size")).dec();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Size +"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.IntSetting) crosshair.getSetting("size")).inc();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Gap -"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.IntSetting) crosshair.getSetting("gap")).dec();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Gap +"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.IntSetting) crosshair.getSetting("gap")).inc();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Thick -"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.IntSetting) crosshair.getSetting("thickness")).dec();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w, h).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Thick +"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.IntSetting) crosshair.getSetting("thickness")).inc();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx + g, y, w, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Dot"), b -> {
            if (crosshair == null) return;
            com.saltclient.setting.BoolSetting dot = (com.saltclient.setting.BoolSetting) crosshair.getSetting("dot");
            dot.toggle();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w * 2 + g * 2, h).build());

        y += h + g;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Color"), b -> {
            if (crosshair == null) return;
            ((com.saltclient.setting.EnumSetting<?>) crosshair.getSetting("color")).next();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - w - g, y, w * 2 + g * 2, h).build());

        y += h + g + 6;

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Back"), b -> close())
            .dimensions(cx - 60, y, 120, 20)
            .build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, TEXT);

        int cx = this.width / 2;
        int cy = this.height / 2;
        drawCrosshair(ctx, cx, cy);

        String info = crosshair == null
            ? "CustomCrosshair module missing"
            : "Preset: " + crosshair.presetName();
        ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text(info), cx, cy + 22, MUTED);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private static void drawCrosshair(DrawContext ctx, int cx, int cy) {
        Module m = SaltClient.MODULES.byId("customcrosshair").orElse(null);
        if (!(m instanceof CustomCrosshairModule crosshair)) return;

        int size = ((com.saltclient.setting.IntSetting) crosshair.getSetting("size")).getValue();
        int gap = ((com.saltclient.setting.IntSetting) crosshair.getSetting("gap")).getValue();
        int t = ((com.saltclient.setting.IntSetting) crosshair.getSetting("thickness")).getValue();
        int c = ((com.saltclient.setting.EnumSetting<com.saltclient.module.impl.crosshair.CrosshairColor>) crosshair.getSetting("color")).getValue().argb;

        ctx.fill(cx - gap - size, cy - t / 2, cx - gap, cy + (t + 1) / 2, c);
        ctx.fill(cx + gap, cy - t / 2, cx + gap + size, cy + (t + 1) / 2, c);
        ctx.fill(cx - t / 2, cy - gap - size, cx + (t + 1) / 2, cy - gap, c);
        ctx.fill(cx - t / 2, cy + gap, cx + (t + 1) / 2, cy + gap + size, c);

        if (((com.saltclient.setting.BoolSetting) crosshair.getSetting("dot")).getValue()) {
            ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, c);
        }
    }
}
