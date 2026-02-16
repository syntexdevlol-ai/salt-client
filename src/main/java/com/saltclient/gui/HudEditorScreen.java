package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.Map;

/**
 * Minimal HUD editor:
 * - Drag visible HUD elements
 * - Right click an element to reset it
 * - Reset All button
 *
 * This is intentionally simple and safe for Pojav.
 */
public final class HudEditorScreen extends Screen {
    private static final int BG = 0x88000000;
    private static final int BORDER = 0xFF7BC96F;
    private static final int BORDER_HOVER = 0xFFFFD66E;
    private static final int TEXT = 0xFFE6ECFF;
    private static final int MUTED = 0xFF9FB0D8;

    private final Screen parent;

    private String draggingId;
    private int dragOffX;
    private int dragOffY;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla blur.
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset All"), b -> {
            HudPos.resetAll();
            SaltClient.CONFIG.save(SaltClient.MODULES);
        }).dimensions(cx - 124, this.height - 34, 110, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(cx + 14, this.height - 34, 110, 20)
            .build());
    }

    @Override
    public boolean shouldPause() {
        return false;
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

        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, TEXT);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Drag HUD elements. Right click = reset."), this.width / 2, 24, MUTED);

        String hoverId = findHoveredId(mouseX, mouseY);

        for (Map.Entry<String, HudPos.Bounds> e : HudPos.allBounds().entrySet()) {
            String id = e.getKey();
            HudPos.Bounds b = e.getValue();
            if (b == null) continue;

            int color = id.equals(hoverId) ? BORDER_HOVER : BORDER;
            ctx.drawBorder(b.x, b.y, b.w, b.h, color);

            // Label
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(id), b.x + 2, b.y + 2, 0xFFFFFFFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private static boolean inside(int mx, int my, HudPos.Bounds b) {
        return mx >= b.x && mx < (b.x + b.w) && my >= b.y && my < (b.y + b.h);
    }

    private String findHoveredId(int mouseX, int mouseY) {
        for (Map.Entry<String, HudPos.Bounds> e : HudPos.allBounds().entrySet()) {
            HudPos.Bounds b = e.getValue();
            if (b != null && inside(mouseX, mouseY, b)) return e.getKey();
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        // Right click = reset hovered element.
        if (button == 1) {
            String id = findHoveredId(mx, my);
            if (id != null) {
                HudPos.clear(id);
                SaltClient.CONFIG.save(SaltClient.MODULES);
                return true;
            }
        }

        for (Map.Entry<String, HudPos.Bounds> e : HudPos.allBounds().entrySet()) {
            HudPos.Bounds b = e.getValue();
            if (b == null) continue;
            if (inside(mx, my, b)) {
                draggingId = e.getKey();
                dragOffX = mx - b.x;
                dragOffY = my - b.y;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingId != null) {
            int mx = (int) mouseX;
            int my = (int) mouseY;

            int nx = mx - dragOffX;
            int ny = my - dragOffY;

            // Basic clamp to screen bounds (keep at least some part visible).
            nx = Math.max(0, Math.min(nx, this.width - 10));
            ny = Math.max(0, Math.min(ny, this.height - 10));

            HudPos.set(draggingId, nx, ny);
            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingId != null) {
            draggingId = null;
            SaltClient.CONFIG.save(SaltClient.MODULES);
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}

