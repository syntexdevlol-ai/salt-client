package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Basic "client menu" inspired by the reference screenshot.
 *
 * Notes:
 * - No fancy rounded corners (kept simple / stable).
 * - Manual list rendering with scrolling.
 */
public final class SaltScreen extends Screen {
    private static final int BG = 0xCC12161F;
    private static final int PANEL = 0xDD1A2130;
    private static final int PANEL_BORDER = 0xFF2B3A55;
    private static final int ROW = 0xFF1E2A3D;
    private static final int ROW_HOVER = 0xFF25344D;
    private static final int ROW_DISABLED = 0xFF182233;
    private static final int TEXT = 0xFFE6ECFF;
    private static final int MUTED = 0xFF9FB0D8;
    private static final int TOGGLE_ON = 0xFF7BC96F;
    private static final int TOGGLE_OFF = 0xFF51607A;

    private TextFieldWidget search;
    private ModuleCategory selected = ModuleCategory.ALL;
    private double scroll;

    public SaltScreen() {
        super(Text.literal("Salt Client"));
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 40, 820);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Search field (top bar)
        this.search = new TextFieldWidget(
            this.textRenderer,
            panelX + 12,
            panelY + 10,
            panelW - 24,
            18,
            Text.literal("Search...")
        );
        this.search.setMaxLength(64);
        // Add as drawable so we can let Screen render it (without calling Screen#renderBackground).
        this.addDrawableChild(this.search);
        this.setInitialFocus(this.search);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // IMPORTANT: we intentionally skip vanilla's renderBackground(), because it calls GameRenderer#renderBlur.
        // Pojav users often want no blur, and it can be expensive.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int panelW = Math.min(this.width - 40, 820);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        // Dim background
        ctx.fill(0, 0, this.width, this.height, BG);

        // Panel
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        // Title
        ctx.drawTextWithShadow(this.textRenderer, this.title, panelX + 12, panelY + 10 + 22, TEXT);

        // Layout
        int topBarH = 54;
        int catW = 140;
        int innerX = panelX + 12;
        int innerY = panelY + topBarH;
        int innerW = panelW - 24;
        int innerH = panelH - topBarH - 12;

        // Category list
        int catX = innerX;
        int catY = innerY;
        int catH = innerH;

        // Module area
        int listX = innerX + catW + 10;
        int listY = innerY;
        int listW = innerW - catW - 10;
        int listH = innerH;

        renderCategories(ctx, mouseX, mouseY, catX, catY, catW, catH);
        renderModules(ctx, mouseX, mouseY, listX, listY, listW, listH);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderCategories(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h) {
        int rowH = 18;
        int yy = y;
        for (ModuleCategory c : ModuleCategory.values()) {
            int bg = (c == selected) ? ROW_HOVER : ROW;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= yy && mouseY < yy + rowH;
            if (hover && c != selected) bg = ROW_HOVER;

            ctx.fill(x, yy, x + w, yy + rowH, bg);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(c.name()), x + 6, yy + 5, TEXT);
            yy += rowH + 4;
            if (yy > y + h - rowH) break;
        }
    }

    private void renderModules(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h) {
        List<Module> list = filteredModules();

        int cols = (w >= 520) ? 2 : 1;
        int gap = 8;
        int rowH = 22;
        int colW = (w - (cols - 1) * gap) / cols;

        int rows = (int) Math.ceil(list.size() / (double) cols);
        int contentH = rows * (rowH + gap);
        int maxScroll = Math.max(0, contentH - h);
        scroll = clamp(scroll, 0, maxScroll);

        // Clip list area (simple manual clipping by draw bounds checks)
        int startY = y - (int) scroll;

        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);

            int row = i / cols;
            int col = i % cols;
            int xx = x + col * (colW + gap);
            int yy = startY + row * (rowH + gap);

            if (yy + rowH < y || yy > y + h) continue;

            boolean hover = mouseX >= xx && mouseX < xx + colW && mouseY >= yy && mouseY < yy + rowH;
            int bg = hover ? ROW_HOVER : ROW;
            if (!m.isImplemented()) bg = hover ? ROW_DISABLED : ROW_DISABLED;

            ctx.fill(xx, yy, xx + colW, yy + rowH, bg);

            // Name
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(m.getName()), xx + 8, yy + 7, TEXT);

            // WIP label
            if (!m.isImplemented()) {
                String tag = "WIP";
                int tagW = this.textRenderer.getWidth(tag);
                ctx.drawTextWithShadow(this.textRenderer, Text.literal(tag), xx + colW - 54 - tagW - 6, yy + 7, MUTED);
            }

            // Toggle
            int toggleW = 42;
            int toggleH = 14;
            int tx = xx + colW - toggleW - 8;
            int ty = yy + (rowH - toggleH) / 2;
            int tbg = m.isEnabled() ? TOGGLE_ON : TOGGLE_OFF;
            ctx.fill(tx, ty, tx + toggleW, ty + toggleH, tbg);

            // Toggle knob
            int knob = 12;
            int kx = m.isEnabled() ? (tx + toggleW - knob - 1) : (tx + 1);
            ctx.fill(kx, ty + 1, kx + knob, ty + toggleH - 1, 0xFF0E121A);
        }
    }

    private List<Module> filteredModules() {
        String q = (search == null) ? "" : search.getText();
        q = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<Module> out = new ArrayList<>();
        for (Module m : SaltClient.MODULES.all()) {
            // If user is searching, show matches across all categories.
            if (q.isEmpty()) {
                if (selected != ModuleCategory.ALL && m.getCategory() != selected) continue;
            } else {
                if (!m.getName().toLowerCase(Locale.ROOT).contains(q)) continue;
            }
            out.add(m);
        }
        return out;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.search != null && this.search.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int panelW = Math.min(this.width - 40, 820);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int topBarH = 54;
        int catW = 140;
        int innerX = panelX + 12;
        int innerY = panelY + topBarH;
        int innerW = panelW - 24;
        int innerH = panelH - topBarH - 12;

        int catX = innerX;
        int catY = innerY;
        int catH = innerH;

        int listX = innerX + catW + 10;
        int listY = innerY;
        int listW = innerW - catW - 10;
        int listH = innerH;

        // Category clicks
        int rowH = 18;
        int yy = catY;
        for (ModuleCategory c : ModuleCategory.values()) {
            boolean inside = mouseX >= catX && mouseX < catX + catW && mouseY >= yy && mouseY < yy + rowH;
            if (inside) {
                selected = c;
                scroll = 0;
                return true;
            }
            yy += rowH + 4;
            if (yy > catY + catH - rowH) break;
        }

        // Module clicks
        if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listY + listH) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<Module> list = filteredModules();
        int cols = (listW >= 520) ? 2 : 1;
        int gap = 8;
        int entryH = 22;
        int colW = (listW - (cols - 1) * gap) / cols;

        double localY = mouseY - listY + scroll;
        int row = (int) (localY / (entryH + gap));
        int col = (int) ((mouseX - listX) / (colW + gap));
        if (col < 0 || col >= cols) return true;

        int idx = row * cols + col;
        if (idx < 0 || idx >= list.size()) return true;

        Module m = list.get(idx);
        if (button == 1) {
            openSettings(m);
        } else {
            // Allow toggling even if WIP (it just won't do anything yet).
            m.toggle();
        }
        return true;
    }

    private void openSettings(Module m) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        mc.setScreen(new ModuleSettingsScreen(this, m));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Scroll modules list if the mouse is over the list area.
        int panelW = Math.min(this.width - 40, 820);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int topBarH = 54;
        int catW = 140;
        int innerX = panelX + 12;
        int innerY = panelY + topBarH;
        int innerW = panelW - 24;
        int innerH = panelH - topBarH - 12;

        int listX = innerX + catW + 10;
        int listY = innerY;
        int listW = innerW - catW - 10;
        int listH = innerH;

        if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
            scroll -= verticalAmount * 18.0;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void removed() {
        super.removed();
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        return Math.min(v, max);
    }
}
