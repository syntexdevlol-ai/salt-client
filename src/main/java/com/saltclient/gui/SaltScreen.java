package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.GuiSettings;
import com.saltclient.util.HudPos;
import com.saltclient.util.UiDraw;
import com.saltclient.util.UiFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SaltScreen extends Screen {
    // Light (Lunar-ish) palette.
    // Glassy dark palette inspired by modern PvP clients.
    private static final int BG = 0xB013233B;
    private static final int PANEL = 0xF2192233;
    private static final int PANEL_BORDER = 0xFF3B82F6;

    private static final int HEADER = 0xFF111827;
    private static final int SIDEBAR = 0xFF0F172A;
    private static final int FOOTER = 0xFF111827;

    private static final int SEARCH_BG = 0xFF111827;
    private static final int SEARCH_BORDER = 0xFF1F2937;

    private static final int SIDEBAR_ROW = 0x00000000;
    private static final int SIDEBAR_ROW_HOVER = 0x26FFFFFF;
    private static final int SIDEBAR_ROW_ACTIVE = 0x332563EB;
    private static final int SIDEBAR_ACCENT = 0xFF38BDF8;

    private static final int CARD = 0xFF111827;
    private static final int CARD_HOVER = 0xFF0B1220;
    private static final int CARD_BORDER = 0xFF1F2937;
    private static final int CARD_DISABLED = 0xFF0B1220;

    private static final int TEXT = 0xFFE2E8F0;
    private static final int MUTED = 0xFFA5B4FC;
    private static final int SUBTLE = 0xFF94A3B8;

    private static final int TOGGLE_ON = 0xFF38BDF8;
    private static final int TOGGLE_OFF = 0xFF1F2937;

    private static final int HEADER_BUTTON = 0xFF1F2937;
    private static final int HEADER_BUTTON_HOVER = 0xFF273548;

    private static final int ACTION_BTN = 0xFF1F2937;
    private static final int ACTION_BTN_HOVER = 0xFF273548;

    private static final int ACTION_SIZE = 22;
    private static final int ACTION_GAP = 6;

    private static final int ACTION_COUNT = 5;

    private static final int POSITIVE = 0xFF8DE39F;
    private static final int NEGATIVE = 0xFFFF8A8A;

    private static final int SCROLL_BTN_SIZE = 20;
    private static final int SCROLL_BTN_PAD = 6;

    private static final int R_PANEL = 14;
    private static final int R_CARD = 12;
    private static final int R_BTN = 10;

    private TextFieldWidget search;
    private TextFieldWidget configName;
    private TextFieldWidget configPath;

    private ModuleCategory selected = ModuleCategory.ALL;
    private boolean configTab;

    private double moduleScroll;
    private double configScroll;

    private final Map<String, Float> toggleAnim = new HashMap<>();
    private List<String> namedConfigs = new ArrayList<>();

    private String statusText = "";
    private int statusColor = MUTED;
    private long statusUntilMs;

    private long openedAtMs;
    private long lastConfigClickMs;
    private String lastConfigClicked = "";

    public SaltScreen() {
        super(UiFonts.text("saltclient"));
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

        int cfgNameX;
        int cfgNameY;
        int cfgNameW;
        int cfgNameH;

        int cfgPathX;
        int cfgPathY;
        int cfgPathW;
        int cfgPathH;

        int cfgSaveX;
        int cfgSaveY;
        int cfgSaveW;
        int cfgSaveH;

        int cfgLoadX;
        int cfgLoadY;
        int cfgLoadW;
        int cfgLoadH;

        int cfgRefreshX;
        int cfgRefreshY;
        int cfgRefreshW;
        int cfgRefreshH;

        int cfgFileLoadX;
        int cfgFileLoadY;
        int cfgFileLoadW;
        int cfgFileLoadH;

        int cfgListX;
        int cfgListY;
        int cfgListW;
        int cfgListH;
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

        int actionTotalW = (ACTION_SIZE * ACTION_COUNT) + (ACTION_GAP * (ACTION_COUNT - 1));
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

        int cfgBtnW = 70;
        int cfgBtnH = 20;
        int cfgGap = 6;
        int cfgLeft = l.listX + 104;

        l.cfgNameY = l.listY + 10;
        l.cfgNameH = 20;
        l.cfgNameX = cfgLeft;
        l.cfgNameW = Math.max(120, l.listW - 110 - (cfgBtnW * 3 + cfgGap * 2) - 12);

        l.cfgSaveX = l.cfgNameX + l.cfgNameW + cfgGap;
        l.cfgSaveY = l.cfgNameY;
        l.cfgSaveW = cfgBtnW;
        l.cfgSaveH = cfgBtnH;

        l.cfgLoadX = l.cfgSaveX + cfgBtnW + cfgGap;
        l.cfgLoadY = l.cfgNameY;
        l.cfgLoadW = cfgBtnW;
        l.cfgLoadH = cfgBtnH;

        l.cfgRefreshX = l.cfgLoadX + cfgBtnW + cfgGap;
        l.cfgRefreshY = l.cfgNameY;
        l.cfgRefreshW = cfgBtnW;
        l.cfgRefreshH = cfgBtnH;

        l.cfgPathY = l.listY + 40;
        l.cfgPathH = 20;
        l.cfgPathX = cfgLeft;
        l.cfgPathW = Math.max(120, l.listW - 110 - cfgBtnW - 12);

        l.cfgFileLoadX = l.cfgPathX + l.cfgPathW + cfgGap;
        l.cfgFileLoadY = l.cfgPathY;
        l.cfgFileLoadW = cfgBtnW;
        l.cfgFileLoadH = cfgBtnH;

        l.cfgListX = l.listX + 10;
        l.cfgListY = l.listY + 74;
        l.cfgListW = l.listW - 20;
        l.cfgListH = l.listH - 84;

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

        this.configName = new TextFieldWidget(
            this.textRenderer,
            l.cfgNameX + 4,
            l.cfgNameY + 5,
            l.cfgNameW - 8,
            12,
            Text.literal("config name")
        );
        this.configName.setMaxLength(64);
        this.configName.setDrawsBackground(false);
        this.configName.setEditableColor(TEXT);
        this.configName.setUneditableColor(MUTED);
        this.configName.setText("default");
        this.addDrawableChild(this.configName);

        this.configPath = new TextFieldWidget(
            this.textRenderer,
            l.cfgPathX + 4,
            l.cfgPathY + 5,
            l.cfgPathW - 8,
            12,
            Text.literal("/sdcard/Download/your-config.json")
        );
        this.configPath.setMaxLength(200);
        this.configPath.setDrawsBackground(false);
        this.configPath.setEditableColor(TEXT);
        this.configPath.setUneditableColor(MUTED);
        this.addDrawableChild(this.configPath);

        this.setInitialFocus(this.search);
        refreshConfigList();
        if (openedAtMs == 0L) openedAtMs = System.currentTimeMillis();
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip vanilla blur.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        Layout l = layout();
        syncWidgets(l);

        float open = openProgress();

        ctx.fill(0, 0, this.width, this.height, scaleAlpha(BG, open));

        UiDraw.panelRounded(ctx, l.panelX, l.panelY, l.panelW, l.panelH, R_PANEL, scaleAlpha(PANEL, open), scaleAlpha(PANEL_BORDER, open));

        int innerX = l.panelX + 1;
        int innerY = l.panelY + 1;
        int innerW = l.panelW - 2;
        int innerH = l.panelH - 2;

        // Header with rounded top corners, squared bottom edge.
        int headerH = Math.min(l.headerH, innerH);
        UiDraw.fillRounded(ctx, innerX, innerY, innerX + innerW, innerY + headerH, Math.max(0, R_PANEL - 1), scaleAlpha(HEADER, open));
        int headerSquareY = Math.max(innerY, innerY + headerH - (R_PANEL - 1));
        ctx.fill(innerX, headerSquareY, innerX + innerW, innerY + headerH, scaleAlpha(HEADER, open));

        // Sidebar box (rounded).
        UiDraw.panelRounded(ctx, l.sidebarX, l.sidebarY, l.sidebarW, l.sidebarH, R_CARD, scaleAlpha(SIDEBAR, open), scaleAlpha(CARD_BORDER, open));

        // Footer with rounded bottom corners, squared top edge.
        int footerY = Math.max(innerY, l.footerY);
        int footerH = innerY + innerH - footerY;
        if (footerH > 0) {
            UiDraw.fillRounded(ctx, innerX, footerY, innerX + innerW, footerY + footerH, Math.max(0, R_PANEL - 1), scaleAlpha(FOOTER, open));
            int footerSquareH = Math.min(footerH, Math.max(0, R_PANEL - 1));
            ctx.fill(innerX, footerY, innerX + innerW, footerY + footerSquareH, scaleAlpha(FOOTER, open));
        }

        ctx.fill(l.panelX, l.panelY + l.headerH, l.panelX + l.panelW, l.panelY + l.headerH + 1, scaleAlpha(0x66CBD5E1, open));
        ctx.fill(l.sidebarX + l.sidebarW + 5, l.contentY, l.sidebarX + l.sidebarW + 6, l.footerY - 2, scaleAlpha(0x40CBD5E1, open));
        ctx.fill(l.panelX, l.footerY - 1, l.panelX + l.panelW, l.footerY, scaleAlpha(0x66CBD5E1, open));

        renderHeader(ctx, mouseX, mouseY, l, open);
        renderSidebar(ctx, mouseX, mouseY, l, open);

        if (configTab) {
            renderConfigManager(ctx, mouseX, mouseY, l, open);
        } else {
            renderModules(ctx, mouseX, mouseY, l, open);
        }

        renderFooter(ctx, mouseX, mouseY, l, open);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderHeader(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        int logoFill = 0xFFE0F2FE;
        int logoBorder = 0xFF93C5FD;
        UiDraw.panelRounded(ctx, l.panelX + 14, l.panelY + 14, 32, 32, R_BTN, scaleAlpha(logoFill, alpha), scaleAlpha(logoBorder, alpha));
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("S"), l.panelX + 27, l.panelY + 25, scaleAlpha(SIDEBAR_ACCENT, alpha));
        ctx.drawTextWithShadow(this.textRenderer, this.title, l.panelX + 54, l.panelY + 22, scaleAlpha(TEXT, alpha));

        UiDraw.panelRounded(ctx, l.searchX, l.searchY, l.searchW, l.searchH, R_BTN, scaleAlpha(SEARCH_BG, alpha), scaleAlpha(SEARCH_BORDER, alpha));

        if (configTab) {
            ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Config section"), l.searchX + 12, l.searchY + 10, scaleAlpha(MUTED, alpha));
        } else {
            ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("?"), l.searchX + 9, l.searchY + 9, scaleAlpha(MUTED, alpha));
        }

        for (int i = 0; i < ACTION_COUNT; i++) {
            int bx = l.actionX + i * (ACTION_SIZE + ACTION_GAP);
            int by = l.actionY;
            boolean hover = inside(mouseX, mouseY, bx, by, ACTION_SIZE, ACTION_SIZE);
            int fill = hover ? HEADER_BUTTON_HOVER : HEADER_BUTTON;
            UiDraw.panelRounded(ctx, bx, by, ACTION_SIZE, ACTION_SIZE, R_BTN, scaleAlpha(fill, alpha), scaleAlpha(SEARCH_BORDER, alpha));

            String glyph = switch (i) {
                case 0 -> "<"; // back
                case 1 -> "I"; // installers
                case 2 -> "M"; // music
                case 3 -> "*"; // misc / configs
                default -> "C"; // chat
            };
            int gx = bx + (ACTION_SIZE - this.textRenderer.getWidth(glyph)) / 2;
            int gy = by + (ACTION_SIZE - this.textRenderer.fontHeight) / 2;
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(glyph), gx, gy, scaleAlpha(TEXT, alpha));
        }
    }

    private void renderSidebar(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        int rowH = 30;
        int gap = 6;
        int y = l.sidebarY + 10;

        for (ModuleCategory c : ModuleCategory.values()) {
            int x = l.sidebarX + 8;
            int w = l.sidebarW - 16;
            boolean hover = inside(mouseX, mouseY, x, y, w, rowH);
            boolean active = !configTab && c == selected;

            int bg = active ? SIDEBAR_ROW_ACTIVE : (hover ? SIDEBAR_ROW_HOVER : SIDEBAR_ROW);
            UiDraw.fillRounded(ctx, x, y, x + w, y + rowH, R_BTN, scaleAlpha(bg, alpha));
            if (active) {
                ctx.fill(x, y, x + 3, y + rowH, scaleAlpha(SIDEBAR_ACCENT, alpha));
            }

            String label = categoryName(c);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(label), x + 12, y + 10, scaleAlpha(TEXT, alpha));
            if (c != ModuleCategory.ALL) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal(">"), x + w - 12, y + 10, scaleAlpha(SUBTLE, alpha));
            }

            y += rowH + gap;
            if (y > l.sidebarY + l.sidebarH - rowH - 8) return;
        }

        int x = l.sidebarX + 8;
        int w = l.sidebarW - 16;
        boolean hover = inside(mouseX, mouseY, x, y, w, rowH);
        boolean active = configTab;

        int bg = active ? SIDEBAR_ROW_ACTIVE : (hover ? SIDEBAR_ROW_HOVER : SIDEBAR_ROW);
        UiDraw.fillRounded(ctx, x, y, x + w, y + rowH, R_BTN, scaleAlpha(bg, alpha));
        if (active) {
            ctx.fill(x, y, x + 3, y + rowH, scaleAlpha(SIDEBAR_ACCENT, alpha));
        }

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Configs"), x + 12, y + 10, scaleAlpha(TEXT, alpha));
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(">"), x + w - 12, y + 10, scaleAlpha(SUBTLE, alpha));
    }

    private void renderModules(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        List<Module> list = filteredModules();

        int cols = (l.listW >= 640) ? 2 : 1;
        int gap = 8;
        int cardH = 64;
        int colW = (l.listW - (cols - 1) * gap) / cols;

        int rows = (int) Math.ceil(list.size() / (double) cols);
        int contentH = Math.max(0, rows * (cardH + gap) - gap);
        int maxScroll = Math.max(0, contentH - l.listH);
        moduleScroll = clamp(moduleScroll, 0, maxScroll);

        int startY = l.listY - (int) moduleScroll;

        for (int i = 0; i < list.size(); i++) {
            Module m = list.get(i);

            int row = i / cols;
            int col = i % cols;
            int x = l.listX + col * (colW + gap);
            int y = startY + row * (cardH + gap);

            if (y + cardH < l.listY || y > l.listY + l.listH) continue;

            boolean hover = inside(mouseX, mouseY, x, y, colW, cardH);
            int bg = m.isImplemented() ? (hover ? CARD_HOVER : CARD) : CARD_DISABLED;

            UiDraw.panelRounded(ctx, x, y, colW, cardH, R_CARD, scaleAlpha(bg, alpha), scaleAlpha(CARD_BORDER, alpha));

            ctx.drawTextWithShadow(this.textRenderer, Text.literal(m.getName()), x + 10, y + 10, scaleAlpha(TEXT, alpha));

            String desc = m.getDescription() == null ? "" : m.getDescription();
            String shortDesc = trimToWidth(desc, colW - 86);
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(shortDesc), x + 10, y + 28, scaleAlpha(MUTED, alpha));

            int tw = 48;
            int th = 18;
            int tx = x + colW - tw - 10;
            int ty = y + 9;

            float toggle = animateToggle(m);
            int toggleColor = blend(TOGGLE_OFF, TOGGLE_ON, toggle);
            UiDraw.panelRounded(ctx, tx, ty, tw, th, th / 2, scaleAlpha(toggleColor, alpha), scaleAlpha(SEARCH_BORDER, alpha));

            int knob = 14;
            int range = tw - knob - 4;
            int kx = tx + 2 + Math.round(range * toggle);
            int ky = ty + 2;
            UiDraw.fillRounded(ctx, kx, ky, kx + knob, ky + knob, knob / 2, scaleAlpha(0xFFFFFFFF, alpha));

            // Settings button (mobile-friendly alternative to right click).
            int sb = 18;
            int sx = x + colW - sb - 10;
            int sy = y + cardH - sb - 10;
            int sFill = hover ? ACTION_BTN_HOVER : ACTION_BTN;
            UiDraw.panelRounded(ctx, sx, sy, sb, sb, R_BTN, scaleAlpha(sFill, alpha), scaleAlpha(SEARCH_BORDER, alpha));
            String sg = "S";
            int sgx = sx + (sb - this.textRenderer.getWidth(sg)) / 2;
            int sgy = sy + (sb - this.textRenderer.fontHeight) / 2;
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(sg), sgx, sgy, scaleAlpha(MUTED, alpha));

            if (!m.isImplemented()) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("WIP"), x + colW - 36, y + 32, scaleAlpha(SUBTLE, alpha));
            }
        }

        renderScrollButtons(ctx, mouseX, mouseY, l, alpha, maxScroll);
    }

    private void renderScrollButtons(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha, int maxScroll) {
        // Mobile-friendly scroll buttons in the footer (always visible).
        int y = l.footerY + (l.footerH - SCROLL_BTN_SIZE) / 2;
        int downX = l.listX + l.listW - SCROLL_BTN_SIZE - SCROLL_BTN_PAD;
        int upX = downX - SCROLL_BTN_SIZE - SCROLL_BTN_PAD;

        boolean canUp = maxScroll > 0 && moduleScroll > 0.5;
        boolean canDown = maxScroll > 0 && moduleScroll < maxScroll - 0.5;

        drawScrollButton(ctx, mouseX, mouseY, upX, y, "^", canUp, alpha);
        drawScrollButton(ctx, mouseX, mouseY, downX, y, "v", canDown, alpha);
    }

    private void drawScrollButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, String glyph, boolean enabled, float alpha) {
        boolean hover = inside(mouseX, mouseY, x, y, SCROLL_BTN_SIZE, SCROLL_BTN_SIZE);

        // Always draw these buttons with enough contrast to be visible on mobile.
        // Even when disabled (nothing to scroll), we still consume clicks to avoid toggling
        // modules behind the buttons, so we give a subtle hover state too.
        int bg;
        if (enabled) bg = hover ? ACTION_BTN_HOVER : ACTION_BTN;
        else bg = hover ? 0xFFE2E8F0 : 0xFFF1F5F9;
        int border = hover ? SIDEBAR_ACCENT : SEARCH_BORDER;
        int fg = enabled ? TEXT : SUBTLE;

        UiDraw.panelRounded(ctx, x, y, SCROLL_BTN_SIZE, SCROLL_BTN_SIZE, R_BTN, scaleAlpha(bg, alpha), scaleAlpha(border, alpha));

        int gx = x + (SCROLL_BTN_SIZE - this.textRenderer.getWidth(glyph)) / 2;
        int gy = y + (SCROLL_BTN_SIZE - this.textRenderer.fontHeight) / 2;
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(glyph), gx, gy, scaleAlpha(fg, alpha));
    }

    private void renderConfigManager(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        UiDraw.panelRounded(ctx, l.listX, l.listY, l.listW, l.listH, R_CARD, scaleAlpha(0xFFF8FAFC, alpha), scaleAlpha(CARD_BORDER, alpha));

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Name"), l.listX + 12, l.cfgNameY + 6, scaleAlpha(TEXT, alpha));
        drawInputShell(ctx, l.cfgNameX, l.cfgNameY, l.cfgNameW, l.cfgNameH, alpha);

        drawActionButton(ctx, mouseX, mouseY, l.cfgSaveX, l.cfgSaveY, l.cfgSaveW, l.cfgSaveH, "Save", alpha);
        drawActionButton(ctx, mouseX, mouseY, l.cfgLoadX, l.cfgLoadY, l.cfgLoadW, l.cfgLoadH, "Load", alpha);
        drawActionButton(ctx, mouseX, mouseY, l.cfgRefreshX, l.cfgRefreshY, l.cfgRefreshW, l.cfgRefreshH, "Refresh", alpha);

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("File"), l.listX + 12, l.cfgPathY + 6, scaleAlpha(TEXT, alpha));
        drawInputShell(ctx, l.cfgPathX, l.cfgPathY, l.cfgPathW, l.cfgPathH, alpha);
        drawActionButton(ctx, mouseX, mouseY, l.cfgFileLoadX, l.cfgFileLoadY, l.cfgFileLoadW, l.cfgFileLoadH, "Load File", alpha);

        ctx.drawTextWithShadow(
            this.textRenderer,
            Text.literal("Double-click profile to load. Use the 'S' button on cards for settings."),
            l.cfgListX,
            l.cfgListY - 11,
            scaleAlpha(MUTED, alpha)
        );

        UiDraw.panelRounded(ctx, l.cfgListX, l.cfgListY, l.cfgListW, l.cfgListH, R_BTN, scaleAlpha(SEARCH_BG, alpha), scaleAlpha(SEARCH_BORDER, alpha));

        int rowH = 22;
        int visibleRows = Math.max(1, l.cfgListH / rowH);
        int maxScroll = Math.max(0, namedConfigs.size() * rowH - l.cfgListH);
        configScroll = clamp(configScroll, 0, maxScroll);

        int startIndex = (int) (configScroll / rowH);
        int yOffset = (int) (configScroll % rowH);

        for (int row = 0; row <= visibleRows; row++) {
            int idx = startIndex + row;
            if (idx >= namedConfigs.size()) break;

            int y = l.cfgListY + row * rowH - yOffset;
            if (y + rowH <= l.cfgListY || y >= l.cfgListY + l.cfgListH) continue;

            String name = namedConfigs.get(idx);
            boolean hover = inside(mouseX, mouseY, l.cfgListX + 2, y, l.cfgListW - 4, rowH);
            boolean selectedName = configName != null && name.equalsIgnoreCase(configName.getText().trim());

            int rowColor;
            if (selectedName) rowColor = 0x260B63F6;
            else rowColor = hover ? 0x140F172A : 0x00000000;

            UiDraw.fillRounded(ctx, l.cfgListX + 2, y, l.cfgListX + l.cfgListW - 2, y + rowH, R_BTN, scaleAlpha(rowColor, alpha));
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(name), l.cfgListX + 10, y + 7, scaleAlpha(TEXT, alpha));
        }

        if (namedConfigs.isEmpty()) {
            ctx.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal("No saved configs yet"),
                l.cfgListX + l.cfgListW / 2,
                l.cfgListY + l.cfgListH / 2 - 4,
                scaleAlpha(MUTED, alpha)
            );
        }

        if (!statusText.isEmpty() && System.currentTimeMillis() < statusUntilMs) {
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(statusText), l.cfgListX, l.cfgListY + l.cfgListH - 12, scaleAlpha(statusColor, alpha));
        }
    }

    private void renderFooter(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        boolean hoverReset = inside(mouseX, mouseY, l.resetX, l.resetY, l.resetW, l.resetH);
        boolean hoverEdit = inside(mouseX, mouseY, l.editX, l.editY, l.editW, l.editH);

        UiDraw.panelRounded(ctx, l.resetX, l.resetY, l.resetW, l.resetH, R_BTN, scaleAlpha(hoverReset ? ACTION_BTN_HOVER : ACTION_BTN, alpha), scaleAlpha(SEARCH_BORDER, alpha));
        UiDraw.panelRounded(ctx, l.editX, l.editY, l.editW, l.editH, R_BTN, scaleAlpha(hoverEdit ? ACTION_BTN_HOVER : ACTION_BTN, alpha), scaleAlpha(SEARCH_BORDER, alpha));

        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("RESET GUI"), l.resetX + l.resetW / 2, l.resetY + 5, scaleAlpha(TEXT, alpha));
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("EDIT HUD"), l.editX + l.editW / 2, l.editY + 5, scaleAlpha(TEXT, alpha));

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("saltclient  AL v1.1.5   Java 21  Fabric"), l.panelX + 10, l.footerY + 28, scaleAlpha(MUTED, alpha));

        String right = statusText();
        int rightX = l.panelX + l.panelW - 10 - this.textRenderer.getWidth(right);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal(right), rightX, l.footerY + 28, scaleAlpha(TEXT, alpha));
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
        Layout l = layout();

        if (!configTab && search != null && search.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (configTab) {
            if (configName != null && configName.mouseClicked(mouseX, mouseY, button)) return true;
            if (configPath != null && configPath.mouseClicked(mouseX, mouseY, button)) return true;
        }

        // Header action buttons
        for (int i = 0; i < ACTION_COUNT; i++) {
            int bx = l.actionX + i * (ACTION_SIZE + ACTION_GAP);
            int by = l.actionY;
            if (!inside(mouseX, mouseY, bx, by, ACTION_SIZE, ACTION_SIZE)) continue;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (i == 0) {
                this.close();
            } else if (i == 1) {
                if (mc != null) mc.setScreen(new InstallerScreen(this));
            } else if (i == 2) {
                if (mc != null) mc.setScreen(new SongPlayerScreen(this));
            } else if (i == 3) {
                // Toggle Config tab
                configTab = !configTab;
                if (configTab) setInitialFocus(configName);
                else setInitialFocus(search);
            } else {
                if (mc != null) mc.setScreen(new GlobalChatScreen());
            }
            return true;
        }

        int rowH = 30;
        int gap = 6;
        int y = l.sidebarY + 10;

        for (ModuleCategory c : ModuleCategory.values()) {
            int x = l.sidebarX + 8;
            int w = l.sidebarW - 16;
            if (inside(mouseX, mouseY, x, y, w, rowH)) {
                configTab = false;
                selected = c;
                moduleScroll = 0;
                setInitialFocus(search);
                return true;
            }
            y += rowH + gap;
            if (y > l.sidebarY + l.sidebarH - rowH - 8) break;
        }

        int configRowX = l.sidebarX + 8;
        int configRowW = l.sidebarW - 16;
        if (inside(mouseX, mouseY, configRowX, y, configRowW, rowH)) {
            configTab = true;
            setInitialFocus(configName);
            return true;
        }

        if (inside(mouseX, mouseY, l.resetX, l.resetY, l.resetW, l.resetH)) {
            HudPos.resetAll();
            SaltClient.CONFIG.save(SaltClient.MODULES);
            setStatus("HUD positions reset", true);
            return true;
        }
        if (inside(mouseX, mouseY, l.editX, l.editY, l.editW, l.editH)) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(new HudEditorScreen(this));
            return true;
        }

        if (configTab) {
            return handleConfigClick(mouseX, mouseY, button, l);
        }

        if (!inside(mouseX, mouseY, l.listX, l.listY, l.listW, l.listH)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        List<Module> list = filteredModules();
        int cols = (l.listW >= 640) ? 2 : 1;
        int gapModules = 8;
        int cardH = 64;
        int colW = (l.listW - (cols - 1) * gapModules) / cols;

        // Mobile-friendly scroll buttons (instead of relying only on mouse wheel / touchpad).
        int rows = (int) Math.ceil(list.size() / (double) cols);
        int contentH = Math.max(0, rows * (cardH + gapModules) - gapModules);
        int maxScroll = Math.max(0, contentH - l.listH);
        int scrollY = l.footerY + (l.footerH - SCROLL_BTN_SIZE) / 2;
        int downX = l.listX + l.listW - SCROLL_BTN_SIZE - SCROLL_BTN_PAD;
        int upX = downX - SCROLL_BTN_SIZE - SCROLL_BTN_PAD;

        // Consume clicks on the scroll buttons even if there's nothing to scroll,
        // so users don't accidentally toggle modules behind them.
        int step = cardH + gapModules;
        if (inside(mouseX, mouseY, upX, scrollY, SCROLL_BTN_SIZE, SCROLL_BTN_SIZE)) {
            if (maxScroll > 0) moduleScroll = clamp(moduleScroll - step, 0, maxScroll);
            return true;
        }
        if (inside(mouseX, mouseY, downX, scrollY, SCROLL_BTN_SIZE, SCROLL_BTN_SIZE)) {
            if (maxScroll > 0) moduleScroll = clamp(moduleScroll + step, 0, maxScroll);
            return true;
        }

        double localY = mouseY - l.listY + moduleScroll;

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
        int cardX = l.listX + col * unitW;
        int cardY = l.listY + row * unitH - (int) moduleScroll;

        // Settings button inside each card (works on mobile).
        int sb = 18;
        int settingsX = cardX + colW - sb - 10;
        int settingsY = cardY + cardH - sb - 10;
        if (inside(mouseX, mouseY, settingsX, settingsY, sb, sb)) {
            openSettings(m);
            return true;
        }

        if (button == 1) {
            openSettings(m);
        } else {
            if (!m.isImplemented()) {
                setStatus("WIP module: " + m.getName(), false);
                return true;
            }
            m.toggle();
        }
        return true;
    }

    private boolean handleConfigClick(double mouseX, double mouseY, int button, Layout l) {
        if (inside(mouseX, mouseY, l.cfgSaveX, l.cfgSaveY, l.cfgSaveW, l.cfgSaveH)) {
            String name = configName == null ? "" : configName.getText();
            boolean ok = SaltClient.CONFIG.saveNamed(SaltClient.MODULES, name);
            if (ok) {
                refreshConfigList();
                setStatus("Saved config: " + name.trim(), true);
            } else {
                setStatus("Save failed. Use a valid name.", false);
            }
            return true;
        }

        if (inside(mouseX, mouseY, l.cfgLoadX, l.cfgLoadY, l.cfgLoadW, l.cfgLoadH)) {
            String name = configName == null ? "" : configName.getText();
            boolean ok = SaltClient.CONFIG.loadNamed(SaltClient.MODULES, name);
            if (ok) {
                setStatus("Loaded config: " + name.trim(), true);
            } else {
                setStatus("Load failed. Profile not found.", false);
            }
            return true;
        }

        if (inside(mouseX, mouseY, l.cfgRefreshX, l.cfgRefreshY, l.cfgRefreshW, l.cfgRefreshH)) {
            refreshConfigList();
            setStatus("Config list refreshed", true);
            return true;
        }

        if (inside(mouseX, mouseY, l.cfgFileLoadX, l.cfgFileLoadY, l.cfgFileLoadW, l.cfgFileLoadH)) {
            String path = configPath == null ? "" : configPath.getText();
            boolean ok = SaltClient.CONFIG.loadExternal(SaltClient.MODULES, path);
            if (ok) {
                setStatus("Loaded file config", true);
            } else {
                setStatus("Load file failed. Check JSON path.", false);
            }
            return true;
        }

        if (!inside(mouseX, mouseY, l.cfgListX, l.cfgListY, l.cfgListW, l.cfgListH)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int rowH = 22;
        int idx = (int) ((mouseY - l.cfgListY + configScroll) / rowH);
        if (idx < 0 || idx >= namedConfigs.size()) return true;

        String name = namedConfigs.get(idx);
        if (configName != null) configName.setText(name);

        if (button == 0) {
            long now = System.currentTimeMillis();
            if (name.equals(lastConfigClicked) && now - lastConfigClickMs <= 350L) {
                boolean ok = SaltClient.CONFIG.loadNamed(SaltClient.MODULES, name);
                setStatus(ok ? "Loaded config: " + name : "Load failed: " + name, ok);
            }
            lastConfigClicked = name;
            lastConfigClickMs = now;
            return true;
        }

        if (button == 1) {
            boolean ok = SaltClient.CONFIG.loadNamed(SaltClient.MODULES, name);
            setStatus(ok ? "Loaded config: " + name : "Load failed: " + name, ok);
            return true;
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

        if (configTab) {
            if (inside(mouseX, mouseY, l.cfgListX, l.cfgListY, l.cfgListW, l.cfgListH)) {
                configScroll -= verticalAmount * 24.0;
                return true;
            }
            return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        if (inside(mouseX, mouseY, l.listX, l.listY, l.listW, l.listH)) {
            moduleScroll -= verticalAmount * 20.0;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    private void refreshConfigList() {
        namedConfigs = SaltClient.CONFIG.listNamedConfigs();
        configScroll = 0.0;
    }

    private void setStatus(String text, boolean success) {
        statusText = text == null ? "" : text;
        statusColor = success ? POSITIVE : NEGATIVE;
        statusUntilMs = System.currentTimeMillis() + 3500L;
    }

    private void syncWidgets(Layout l) {
        if (search != null) {
            search.setX(l.searchX + 26);
            search.setY(l.searchY + 6);
            search.setWidth(l.searchW - 34);
            search.setVisible(!configTab);
            search.setEditable(!configTab);
        }

        if (configName != null) {
            configName.setX(l.cfgNameX + 4);
            configName.setY(l.cfgNameY + 5);
            configName.setWidth(l.cfgNameW - 8);
            configName.setVisible(configTab);
            configName.setEditable(configTab);
        }

        if (configPath != null) {
            configPath.setX(l.cfgPathX + 4);
            configPath.setY(l.cfgPathY + 5);
            configPath.setWidth(l.cfgPathW - 8);
            configPath.setVisible(configTab);
            configPath.setEditable(configTab);
        }
    }

    private void drawInputShell(DrawContext ctx, int x, int y, int w, int h, float alpha) {
        UiDraw.panelRounded(ctx, x, y, w, h, R_BTN, scaleAlpha(SEARCH_BG, alpha), scaleAlpha(SEARCH_BORDER, alpha));
    }

    private void drawActionButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h, String label, float alpha) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        UiDraw.panelRounded(ctx, x, y, w, h, R_BTN, scaleAlpha(hover ? ACTION_BTN_HOVER : ACTION_BTN, alpha), scaleAlpha(SEARCH_BORDER, alpha));
        int ty = y + (h - this.textRenderer.fontHeight) / 2;
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), x + w / 2, ty, scaleAlpha(TEXT, alpha));
    }

    private float animateToggle(Module module) {
        float target = module.isEnabled() ? 1.0f : 0.0f;
        float current = toggleAnim.getOrDefault(module.getId(), target);
        current += (target - current) * 0.3f;
        if (Math.abs(target - current) < 0.01f) current = target;
        toggleAnim.put(module.getId(), current);
        return current;
    }

    private float openProgress() {
        if (!GuiSettings.animationsEnabled()) return 1.0f;
        int speed = Math.max(1, GuiSettings.animationSpeedMs());
        float linear = (System.currentTimeMillis() - openedAtMs) / (float) speed;
        linear = (float) clamp(linear, 0.0, 1.0);
        float inv = 1.0f - linear;
        return 1.0f - inv * inv * inv;
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

    private static int scaleAlpha(int color, float amount) {
        int a = (color >>> 24) & 0xFF;
        int scaled = Math.max(0, Math.min(255, Math.round(a * amount)));
        return (color & 0x00FFFFFF) | (scaled << 24);
    }

    private static int blend(int from, int to, float t) {
        t = (float) clamp(t, 0.0, 1.0);

        int a1 = (from >>> 24) & 0xFF;
        int r1 = (from >>> 16) & 0xFF;
        int g1 = (from >>> 8) & 0xFF;
        int b1 = from & 0xFF;

        int a2 = (to >>> 24) & 0xFF;
        int r2 = (to >>> 16) & 0xFF;
        int g2 = (to >>> 8) & 0xFF;
        int b2 = to & 0xFF;

        int a = Math.round(a1 + (a2 - a1) * t);
        int r = Math.round(r1 + (r2 - r1) * t);
        int g = Math.round(g1 + (g2 - g1) * t);
        int b = Math.round(b1 + (b2 - b1) * t);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        return Math.min(v, max);
    }
}
