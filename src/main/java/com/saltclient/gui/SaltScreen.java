package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.HudPos;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Main Salt menu inspired by the reference UI.
 */
public final class SaltScreen extends Screen {
    private static final int BG = 0xA8060A14;
    private static final int PANEL = 0xE0101626;
    private static final int PANEL_BORDER = 0xFF2A3553;

    private static final int HEADER = 0xCC1A2034;
    private static final int SIDEBAR = 0xB2161C2E;
    private static final int FOOTER = 0xCC13192B;

    private static final int SEARCH_BG = 0x99232B43;
    private static final int SEARCH_BORDER = 0xFF344266;

    private static final int SIDEBAR_ROW = 0x4427314B;
    private static final int SIDEBAR_ROW_HOVER = 0x66303C59;
    private static final int SIDEBAR_ROW_ACTIVE = 0xAA36508A;
    private static final int SIDEBAR_ACCENT = 0xFF4DA0FF;

    private static final int CARD = 0xAA1C2438;
    private static final int CARD_HOVER = 0xCC22304B;
    private static final int CARD_BORDER = 0xFF2B3959;
    private static final int CARD_DISABLED = 0xAA141C2B;

    private static final int TEXT = 0xFFE9EEFF;
    private static final int MUTED = 0xFFA1AFD4;
    private static final int SUBTLE = 0xFF7F8DB1;

    private static final int TOGGLE_ON = 0xFF4DA0FF;
    private static final int TOGGLE_OFF = 0xFF42506D;

    private static final int HEADER_BUTTON = 0xAA303B59;
    private static final int HEADER_BUTTON_HOVER = 0xCC3A496B;

    private static final int ACTION_BTN = 0xAA2A334D;
    private static final int ACTION_BTN_HOVER = 0xCC364463;

    private static final int ACTION_SIZE = 22;
    private static final int ACTION_GAP = 6;

    private TextFieldWidget search;
    private ModuleCategory selected = ModuleCategory.ALL;
    private double scroll;

    public SaltScreen() {
        super(Text.literal("saltclient"));
    }

    private static final class Layout {
        int panelX;
        int panelY;
        int panelW;
        int panelH;

        int headerH;
        int footerH;

        int searchX;
        int searchY;
        int searchW;
        int searchH;

        int actionX;
        int actionY;

        int contentY;
        int contentH;

        int sidebarX;
        int sidebarY;
        int sidebarW;
        int sidebarH;

        int listX;
        int listY;
        int listW;
        int listH;

        int footerY;

        int resetX;
        int resetY;
        int resetW;
        int resetH;

        int editX;
        int editY;
        int editW;
        int editH;
    }

    private Layout layout() {
        Layout l = new Layout();

        l.panelW = Math.min(this.width - 36, 1000);
        l.panelH = Math.min(this.height - 36, 560);
        l.panelX = (this.width - l.panelW) / 2;
        l.panelY = (this.height - l.panelH) / 2;

        l.headerH = 56;
        l.footerH = 40;

        int headerPad = 12;
        int brandW = 220;

        int actionTotalW = (ACTION_SIZE * 3) + (ACTION_GAP * 2);
        l.actionX = l.panelX + l.panelW - headerPad - actionTotalW;
        l.actionY = l.panelY + (l.headerH - ACTION_SIZE) / 2;

        l.searchX = l.panelX + brandW + 8;
        l.searchY = l.panelY + 14;
        l.searchH = 28;
        l.searchW = l.actionX - 10 - l.searchX;

        l.contentY = l.panelY + l.headerH + 8;
        l.footerY = l.panelY + l.panelH - l.footerH;
        l.contentH = l.footerY - l.contentY - 8;

        l.sidebarX = l.panelX + 10;
        l.sidebarY = l.contentY;
        l.sidebarW = Math.min(250, Math.max(190, l.panelW / 4));
        l.sidebarH = l.contentH;

        l.listX = l.sidebarX + l.sidebarW + 10;
        l.listY = l.contentY;
        l.listW = l.panelX + l.panelW - 10 - l.listX;
        l.listH = l.contentH;

        int actionGap = 6;
        int actionPad = 8;
        int actionW = (l.sidebarW - actionPad * 2 - actionGap) / 2;
        int actionH = 18;

        l.resetX = l.sidebarX + actionPad;
        l.resetY = l.footerY + (l.footerH - actionH) / 2;
        l.resetW = actionW;
        l.resetH = actionH;

        l.editX = l.resetX + actionW + actionGap;
        l.editY = l.resetY;
        l.editW = actionW;
        l.editH = actionH;

        return l;
    }

    @Override
    protected void init() {
        Layout l = layout();

        this.search = new TextFieldWidget(
            this.textRenderer,
            l.searchX + 26,
            l.searchY + 6,
            l.searchW - 34,
            16,
            Text.literal("Search...")
        );
        this.search.setMaxLength(64);
        this.search.setDrawsBackground(false);
        this.search.setEditableColor(TEXT);
        this.search.setUneditableColor(MUTED);
        this.addDrawableChild(this.search);
        this.setInitialFocus(this.search);
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Intentionally skip vanilla blur.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        Layout l = layout();

        ctx.fill(0, 0, this.width, this.height, BG);

        // Main shell
        ctx.fill(l.panelX, l.panelY, l.panelX + l.panelW, l.panelY + l.panelH, PANEL);
        ctx.drawBorder(l.panelX, l.panelY, l.panelW, l.panelH, PANEL_BORDER);

        // Header / content / footer sections
        ctx.fill(l.panelX, l.panelY, l.panelX + l.panelW, l.panelY + l.headerH, HEADER);
        ctx.fill(l.sidebarX, l.sidebarY, l.sidebarX + l.sidebarW, l.sidebarY + l.sidebarH, SIDEBAR);
        ctx.fill(l.panelX, l.footerY, l.panelX + l.panelW, l.panelY + l.panelH, FOOTER);

        // Section separators
        ctx.fill(l.panelX, l.panelY + l.headerH, l.panelX + l.panelW, l.panelY + l.headerH + 1, 0x55384A73);
        ctx.fill(l.sidebarX + l.sidebarW + 5, l.contentY, l.sidebarX + l.sidebarW + 6, l.footerY - 2, 0x33384A73);
        ctx.fill(l.panelX, l.footerY - 1, l.panelX + l.panelW, l.footerY, 0x55384A73);

        renderHeader(ctx, mouseX, mouseY, l);
        renderSidebar(ctx, mouseX, mouseY, l);
        renderModules(ctx, mouseX, mouseY, l);
        renderFooter(ctx, mouseX, mouseY, l);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext ctx, int mouseX, int mouseY, Layout l) {
        // Brand
        ctx.fill(l.panelX + 14, l.panelY + 14, l.panelX + 46, l.panelY + 46, 0x9942537B);
        ctx.drawBorder(l.panelX + 14, l.panelY + 14, 32, 32, 0xFF566A97);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("S"), l.panelX + 27, l.panelY + 25, TEXT);
        ctx.drawTextWithShadow(this.textRenderer, this.title, l.panelX + 54, l.panelY + 22, TEXT);

        // Search
        ctx.fill(l.searchX, l.searchY, l.searchX + l.searchW, l.searchY + l.searchH, SEARCH_BG);
        ctx.drawBorder(l.searchX, l.searchY, l.searchW, l.searchH, SEARCH_BORDER);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("?"), l.searchX + 9, l.searchY + 9, MUTED);

        // Header action buttons (visual)
        for (int i = 0; i < 3; i++) {
            int bx = l.actionX + i * (ACTION_SIZE + ACTION_GAP);
            int by = l.actionY;
            boolean hover = inside(mouseX, mouseY, bx, by, ACTION_SIZE, ACTION_SIZE);
            ctx.fill(bx, by, bx + ACTION_SIZE, by + ACTION_SIZE, hover ? HEADER_BUTTON_HOVER : HEADER_BUTTON);
            ctx.drawBorder(bx, by, ACTION_SIZE, ACTION_SIZE, 0xFF4A5C88);

            String glyph = (i == 0) ? "<" : (i == 1 ? "*" : "U");
            int gx = bx + (ACTION_SIZE - this.textRenderer.getWidth(glyph)) / 2;
            int gy = by + (ACTION_SIZE - this.textRenderer.fontHeight) / 2;
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(glyph), gx, gy, TEXT);
        }
    }

    private void renderSidebar(DrawContext ctx, int mouseX, int mouseY, Layout l) {
        int rowH = 30;
        int gap = 6;
        int y = l.sidebarY + 10;

        ModuleCategory[] cats = ModuleCategory.values();
        for (ModuleCategory c : cats) {
            int x = l.sidebarX + 8;
            int w = l.sidebarW - 16;
            boolean hover = inside(mouseX, mouseY, x, y, w, rowH);
            boolean active = c == selected;

            int bg = active ? SIDEBAR_ROW_ACTIVE : (hover ? SIDEBAR_ROW_HOVER : SIDEBAR_ROW);
            ctx.fill(x, y, x + w, y + rowH, bg);
            if (active) {
                ctx.fill(x, y, x + 3, y + rowH, SIDEBAR_ACCENT);
            }

            String label = categoryName(c);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 12, y + 10, TEXT);
            if (c != ModuleCategory.ALL) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal(">"), x + w - 12, y + 10, SUBTLE);
            }

            y += rowH + gap;
            if (y > l.sidebarY + l.sidebarH - rowH - 4) break;
        }
    }

    private void renderModules(DrawContext ctx, int mouseX, int mouseY, Layout l) {
        List<Module> list = filteredModules();

        int cols = (l.listW >= 640) ? 2 : 1;
        int gap = 8;
        int cardH = 64;
        int colW = (l.listW - (cols - 1) * gap) / cols;

        int rows = (int) Math.ceil(list.size() / (double) cols);
        int contentH = Math.max(0, rows * (cardH + gap) - gap);
        int maxScroll = Math.max(0, contentH - l.listH);
        scroll = clamp(scroll, 0, maxScroll);

        int startY = l.listY - (int) scroll;

        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);

            int row = i / cols;
            int col = i % cols;
            int x = l.listX + col * (colW + gap);
            int y = startY + row * (cardH + gap);

            if (y + cardH < l.listY || y > l.listY + l.listH) continue;

            boolean hover = inside(mouseX, mouseY, x, y, colW, cardH);
            int bg = m.isImplemented() ? (hover ? CARD_HOVER : CARD) : CARD_DISABLED;

            ctx.fill(x, y, x + colW, y + cardH, bg);
            ctx.drawBorder(x, y, colW, cardH, CARD_BORDER);

            String title = m.getName();
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(title), x + 10, y + 10, TEXT);

            String desc = m.getDescription() == null ? "" : m.getDescription();
            String shortDesc = trimToWidth(desc, colW - 86);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(shortDesc), x + 10, y + 28, MUTED);

            // Toggle
            int tw = 48;
            int th = 18;
            int tx = x + colW - tw - 10;
            int ty = y + 9;
            ctx.fill(tx, ty, tx + tw, ty + th, m.isEnabled() ? TOGGLE_ON : TOGGLE_OFF);
            ctx.drawBorder(tx, ty, tw, th, 0xFF45587C);

            int knob = 14;
            int kx = m.isEnabled() ? tx + tw - knob - 2 : tx + 2;
            int ky = ty + 2;
            ctx.fill(kx, ky, kx + knob, ky + knob, 0xFFE7ECFF);

            if (!m.isImplemented()) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("WIP"), x + colW - 36, y + 32, SUBTLE);
            } else if (m.hasSettings()) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("*"), x + colW - 12, y + 32, SUBTLE);
            }
        }
    }

    private void renderFooter(DrawContext ctx, int mouseX, int mouseY, Layout l) {
        // Action buttons (left)
        boolean hoverReset = inside(mouseX, mouseY, l.resetX, l.resetY, l.resetW, l.resetH);
        boolean hoverEdit = inside(mouseX, mouseY, l.editX, l.editY, l.editW, l.editH);

        ctx.fill(l.resetX, l.resetY, l.resetX + l.resetW, l.resetY + l.resetH, hoverReset ? ACTION_BTN_HOVER : ACTION_BTN);
        ctx.fill(l.editX, l.editY, l.editX + l.editW, l.editY + l.editH, hoverEdit ? ACTION_BTN_HOVER : ACTION_BTN);
        ctx.drawBorder(l.resetX, l.resetY, l.resetW, l.resetH, 0xFF4A5D86);
        ctx.drawBorder(l.editX, l.editY, l.editW, l.editH, 0xFF4A5D86);

        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("RESET GUI"), l.resetX + l.resetW / 2, l.resetY + 5, TEXT);
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("EDIT HUD"), l.editX + l.editW / 2, l.editY + 5, TEXT);

        // Footer text
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("saltclient  AL v1.0.0   Java 21  Fabric"), l.panelX + 10, l.footerY + 28, MUTED);

        String right = statusText();
        int rightX = l.panelX + l.panelW - 10 - this.textRenderer.getWidth(right);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(right), rightX, l.footerY + 28, TEXT);
    }

    private String statusText() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int fps = mc == null ? 0 : mc.getCurrentFps();

        int ping = -1;
        if (mc != null && mc.player != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }

        if (ping >= 0) return fps + " FPS   " + ping + "ms";
        return fps + " FPS";
    }

    private static String categoryName(ModuleCategory c) {
        return switch (c) {
            case ALL -> "Mods";
            case HUD -> "HUD";
            case CHAT -> "Chat";
            case CAMERA -> "Camera";
            case CROSSHAIR -> "Crosshair";
            case VISUAL -> "Visual";
            case PERFORMANCE -> "Performance";
            case MOVEMENT -> "Movement";
            case COMBAT -> "Combat";
            case MISC -> "Misc";
        };
    }

    private List<Module> filteredModules() {
        String q = (search == null) ? "" : search.getText();
        q = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<Module> out = new ArrayList<>();
        for (Module m : SaltClient.MODULES.all()) {
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

        Layout l = layout();

        // Header back button
        if (inside(mouseX, mouseY, l.actionX, l.actionY, ACTION_SIZE, ACTION_SIZE)) {
            this.close();
            return true;
        }

        // Sidebar category clicks
        int rowH = 30;
        int gap = 6;
        int y = l.sidebarY + 10;
        for (ModuleCategory c : ModuleCategory.values()) {
            int x = l.sidebarX + 8;
            int w = l.sidebarW - 16;
            if (inside(mouseX, mouseY, x, y, w, rowH)) {
                selected = c;
                scroll = 0;
                return true;
            }

            y += rowH + gap;
            if (y > l.sidebarY + l.sidebarH - rowH - 4) break;
        }

        // Footer action buttons
        if (inside(mouseX, mouseY, l.resetX, l.resetY, l.resetW, l.resetH)) {
            HudPos.resetAll();
            SaltClient.CONFIG.save(SaltClient.MODULES);
            return true;
        }
        if (inside(mouseX, mouseY, l.editX, l.editY, l.editW, l.editH)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(new HudEditorScreen(this));
            return true;
        }

        // Module clicks
        if (!inside(mouseX, mouseY, l.listX, l.listY, l.listW, l.listH)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<Module> list = filteredModules();
        int cols = (l.listW >= 640) ? 2 : 1;
        int gapModules = 8;
        int cardH = 64;
        int colW = (l.listW - (cols - 1) * gapModules) / cols;

        double localY = mouseY - l.listY + scroll;

        int unitW = colW + gapModules;
        int unitH = cardH + gapModules;
        int col = (int) ((mouseX - l.listX) / unitW);
        int row = (int) (localY / unitH);

        if (col < 0 || col >= cols || row < 0) return true;

        double inCol = (mouseX - l.listX) - (col * unitW);
        double inRow = localY - (row * unitH);
        if (inCol < 0 || inCol >= colW || inRow < 0 || inRow >= cardH) return true;

        int idx = row * cols + col;
        if (idx < 0 || idx >= list.size()) return true;

        Module m = list.get(idx);
        if (button == 1) {
            openSettings(m);
        } else {
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
        Layout l = layout();
        if (inside(mouseX, mouseY, l.listX, l.listY, l.listW, l.listH)) {
            scroll -= verticalAmount * 20.0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private String trimToWidth(String text, int width) {
        if (text == null || text.isEmpty()) return "";
        if (this.textRenderer.getWidth(text) <= width) return text;

        String suffix = "...";
        int suffixW = this.textRenderer.getWidth(suffix);
        if (suffixW >= width) return suffix;

        int end = text.length();
        while (end > 0 && this.textRenderer.getWidth(text.substring(0, end)) + suffixW > width) {
            end--;
        }
        return text.substring(0, Math.max(0, end)) + suffix;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        return Math.min(v, max);
    }
}
