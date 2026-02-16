package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import com.saltclient.setting.KeySetting;
import com.saltclient.setting.Setting;
import com.saltclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Generic module settings UI (right click a module in the menu).
 *
 * Supports:
 * - BoolSetting (toggle)
 * - IntSetting ( +/- )
 * - EnumSetting (cycle)
 * - KeySetting (bind/unbind)
 *
 * This stays intentionally simple (test client UI).
 */
public final class ModuleSettingsScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int PANEL = 0xE0131A27;
    private static final int PANEL_BORDER = 0xFF23324A;
    private static final int TEXT = 0xFFE6EBFA;
    private static final int MUTED = 0xFF8EA1C8;

    private final Screen parent;
    private final Module module;

    private KeySetting listeningKey;

    public ModuleSettingsScreen(Screen parent, Module module) {
        super(Text.literal(module.getName() + " Settings"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla blur.
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int panelW = Math.min(this.width - 40, 520);
        int panelX = (this.width - panelW) / 2;
        int y = (this.height - 260) / 2 + 60;

        int rowH = 20;
        int g = 6;

        List<Setting<?>> settings = module.getSettings();
        for (Setting<?> s : settings) {
            if (y > this.height - 70) break;
            addRow(panelX + 12, y, panelW - 24, rowH, g, s);
            y += rowH + g;
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(cx - 60, this.height - 34, 120, 20)
            .build());
    }

    private void addRow(int x, int y, int w, int h, int g, Setting<?> s) {
        int labelW = Math.min(220, w / 2);
        int btnW = 70;

        // Setting name (drawn manually in render)
        // Controls:
        int rightX = x + w - btnW;

        if (s instanceof BoolSetting bs) {
            addDrawableChild(ButtonWidget.builder(toggleText(bs), b -> {
                bs.toggle();
                b.setMessage(toggleText(bs));
                SaltClient.CONFIG.save(SaltClient.MODULES);
            }).dimensions(rightX, y, btnW, h).build());
            return;
        }

        if (s instanceof KeySetting ks) {
            // Bind button
            addDrawableChild(ButtonWidget.builder(bindText(ks), b -> {
                listeningKey = ks;
                b.setMessage(Text.literal("Press..."));
            }).dimensions(rightX - (btnW + g), y, btnW, h).build());

            // Unbind
            addDrawableChild(ButtonWidget.builder(Text.literal("Clear"), b -> {
                ks.setValue(-1);
                listeningKey = null;
                SaltClient.CONFIG.save(SaltClient.MODULES);
                // Force a rebuild so button labels refresh.
                this.clearAndInit();
            }).dimensions(rightX, y, btnW, h).build());
            return;
        }

        if (s instanceof IntSetting is) {
            addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> {
                is.dec();
                SaltClient.CONFIG.save(SaltClient.MODULES);
                this.clearAndInit();
            }).dimensions(rightX - (btnW * 2 + g), y, btnW, h).build());

            ButtonWidget val = addDrawableChild(ButtonWidget.builder(Text.literal(String.valueOf(is.getValue())), b -> {})
                .dimensions(rightX - (btnW + g), y, btnW, h)
                .build());
            val.active = false;

            addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
                is.inc();
                SaltClient.CONFIG.save(SaltClient.MODULES);
                this.clearAndInit();
            }).dimensions(rightX, y, btnW, h).build());
            return;
        }

        if (s instanceof EnumSetting<?> es) {
            addDrawableChild(ButtonWidget.builder(Text.literal(String.valueOf(es.getValue())), b -> {
                es.next();
                SaltClient.CONFIG.save(SaltClient.MODULES);
                this.clearAndInit();
            }).dimensions(rightX, y, btnW, h).build());
        }
    }

    private static Text toggleText(BoolSetting s) {
        return Text.literal(s.getValue() ? "ON" : "OFF");
    }

    private static Text bindText(KeySetting ks) {
        int code = ks.getValue();
        if (code < 0) return Text.literal("Bind");
        return InputUtil.fromKeyCode(code, 0).getLocalizedText();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningKey != null) {
            // ESC clears "listening" without changing the bind.
            if (keyCode == 256 /* GLFW_KEY_ESCAPE */) {
                listeningKey = null;
                this.clearAndInit();
                return true;
            }

            listeningKey.setValue(keyCode);
            listeningKey = null;
            SaltClient.CONFIG.save(SaltClient.MODULES);
            this.clearAndInit();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && parent != null) {
            mc.setScreen(parent);
        } else {
            super.close();
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 520);
        int panelH = Math.min(this.height - 40, 320);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, panelY + 16, TEXT);

        String desc = module.getDescription() == null ? "" : module.getDescription();
        if (!desc.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(desc), this.width / 2, panelY + 34, MUTED);
        }

        // Draw setting labels aligned to where we placed widgets.
        int x = panelX + 12;
        int y = panelY + 60;
        int rowH = 20;
        int g = 6;
        for (Setting<?> s : module.getSettings()) {
            if (y > panelY + panelH - 70) break;
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(s.getName()), x, y + 6, TEXT);
            y += rowH + g;
        }

        if (listeningKey != null) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Press a key... (ESC to cancel)"), this.width / 2, panelY + panelH - 56, MUTED);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }
}
