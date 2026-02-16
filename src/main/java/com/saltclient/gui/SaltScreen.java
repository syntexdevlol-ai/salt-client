package com.saltclient.gui;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.util.GuiSettings;
import com.saltclient.util.HudPos;
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

    private static final int POSITIVE = 0xFF8DE39F;
    private static final int NEGATIVE = 0xFFFF8A8A;

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

        ctx.fill(l.panelX, l.panelY, l.panelX + l.panelW, l.panelY + l.panelH, scaleAlpha(PANEL, open));
        ctx.drawBorder(l.panelX, l.panelY, l.panelW, l.panelH, scaleAlpha(PANEL_BORDER, open));

        ctx.fill(l.panelX, l.panelY, l.panelX + l.panelW, l.panelY + l.headerH, scaleAlpha(HEADER, open));
        ctx.fill(l.sidebarX, l.sidebarY, l.sidebarX + l.sidebarW, l.sidebarY + l.sidebarH, scaleAlpha(SIDEBAR, open));
        ctx.fill(l.panelX, l.footerY, l.panelX + l.panelW, l.panelY + l.panelH, scaleAlpha(FOOTER, open));

        ctx.fill(l.panelX, l.panelY + l.headerH, l.panelX + l.panelW, l.panelY + l.headerH + 1, scaleAlpha(0x55384A73, open));
        ctx.fill(l.sidebarX + l.sidebarW + 5, l.contentY, l.sidebarX + l.sidebarW + 6, l.footerY - 2, scaleAlpha(0x33384A73, open));
        ctx.fill(l.panelX, l.footerY - 1, l.panelX + l.panelW, l.footerY, scaleAlpha(0x55384A73, open));

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
        ctx.fill(l.panelX + 14, l.panelY + 14, l.panelX + 46, l.panelY + 46, scaleAlpha(0x9942537B, alpha));
        ctx.drawBorder(l.panelX + 14, l.panelY + 14, 32, 32, scaleAlpha(0xFF566A97, alpha));
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("S"), l.panelX + 27, l.panelY + 25, scaleAlpha(TEXT, alpha));
        ctx.drawTextWithShadow(this.textRenderer, this.title, l.panelX + 54, l.panelY + 22, scaleAlpha(TEXT, alpha));

        ctx.fill(l.searchX, l.searchY, l.searchX + l.searchW, l.searchY + l.searchH, scaleAlpha(SEARCH_BG, alpha));
        ctx.drawBorder(l.searchX, l.searchY, l.searchW, l.searchH, scaleAlpha(SEARCH_BORDER, alpha));

        if (configTab) {
            ctx.drawTextWithShadow(this.textRenderer, Text.literal("Config section"), l.searchX + 12, l.searchY + 10, scaleAlpha(MUTED, alpha));
        } else {
            ctx.drawTextWithShadow(this.textRenderer, Text.literal("?"), l.searchX + 9, l.searchY + 9, scaleAlpha(MUTED, alpha));
        }

        for (int i = 0; i < 3; i++) {
            int bx = l.actionX + i * (ACTION_SIZE + ACTION_GAP);
            int by = l.actionY;
            boolean hover = inside(mouseX, mouseY, bx, by, ACTION_SIZE, ACTION_SIZE);
            int fill = hover ? HEADER_BUTTON_HOVER : HEADER_BUTTON;
            ctx.fill(bx, by, bx + ACTION_SIZE, by + ACTION_SIZE, scaleAlpha(fill, alpha));
            ctx.drawBorder(bx, by, ACTION_SIZE, ACTION_SIZE, scaleAlpha(0xFF4A5C88, alpha));

            String glyph = (i == 0) ? "<" : (i == 1 ? "*" : "U");
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
            ctx.fill(x, y, x + w, y + rowH, scaleAlpha(bg, alpha));
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
        ctx.fill(x, y, x + w, y + rowH, scaleAlpha(bg, alpha));
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

            ctx.fill(x, y, x + colW, y + cardH, scaleAlpha(bg, alpha));
            ctx.drawBorder(x, y, colW, cardH, scaleAlpha(CARD_BORDER, alpha));

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
            ctx.fill(tx, ty, tx + tw, ty + th, scaleAlpha(toggleColor, alpha));
            ctx.drawBorder(tx, ty, tw, th, scaleAlpha(0xFF45587C, alpha));

            int knob = 14;
            int range = tw - knob - 4;
            int kx = tx + 2 + Math.round(range * toggle);
            int ky = ty + 2;
            ctx.fill(kx, ky, kx + knob, ky + knob, scaleAlpha(0xFFE7ECFF, alpha));

            if (!m.isImplemented()) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("WIP"), x + colW - 36, y + 32, scaleAlpha(SUBTLE, alpha));
            } else if (m.hasSettings()) {
                ctx.drawTextWithShadow(this.textRenderer, Text.literal("*"), x + colW - 12, y + 32, scaleAlpha(SUBTLE, alpha));
            }
        }
    }

    private void renderConfigManager(DrawContext ctx, int mouseX, int mouseY, Layout l, float alpha) {
        ctx.fill(l.listX, l.listY, l.listX + l.listW, l.listY + l.listH, scaleAlpha(0x551A2238, alpha));
        ctx.drawBorder(l.listX, l.listY, l.listW, l.listH, scaleAlpha(CARD_BORDER, alpha));

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
            Text.literal("Double-click profile to load. Right-click any module card for settings."),
            l.cfgListX,
            l.cfgListY - 11,
            scaleAlpha(MUTED, alpha)
        );

        ctx.fill(l.cfgListX, l.cfgListY, l.cfgListX + l.cfgListW, l.cfgListY + l.cfgListH, scaleAlpha(0x66131A2D, alpha));
        ctx.drawBorder(l.cfgListX, l.cfgListY, l.cfgListW, l.cfgListH, scaleAlpha(0xFF334568, alpha));

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
            if (selectedName) rowColor = 0xAA35518A;
            else rowColor = hover ? 0x66304A78 : 0x3322304A;

            ctx.fill(l.cfgListX + 2, y, l.cfgListX + l.cfgListW - 2, y + rowH, scaleAlpha(rowColor, alpha));
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

        ctx.fill(l.resetX, l.resetY, l.resetX + l.resetW, l.resetY + l.resetH, scaleAlpha(hoverReset ? ACTION_BTN_HOVER : ACTION_BTN, alpha));
        ctx.fill(l.editX, l.editY, l.editX + l.editW, l.editY + l.editH, scaleAlpha(hoverEdit ? ACTION_BTN_HOVER : ACTION_BTN, alpha));
        ctx.drawBorder(l.resetX, l.resetY, l.resetW, l.resetH, scaleAlpha(0xFF4A5D86, alpha));
        ctx.drawBorder(l.editX, l.editY, l.editW, l.editH, scaleAlpha(0xFF4A5D86, alpha));

        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("RESET GUI"), l.resetX + l.resetW / 2, l.resetY + 5, scaleAlpha(TEXT, alpha));
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("EDIT HUD"), l.editX + l.editW / 2, l.editY + 5, scaleAlpha(TEXT, alpha));

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("saltclient  AL v1.0.0   Java 21  Fabric"), l.panelX + 10, l.footerY + 28, scaleAlpha(MUTED, alpha));

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

        if (inside(mouseX, mouseY, l.actionX, l.actionY, ACTION_SIZE, ACTION_SIZE)) {
            this.close();
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
        if (button == 1) {
            openSettings(m);
        } else {
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
        ctx.fill(x, y, x + w, y + h, scaleAlpha(SEARCH_BG, alpha));
        ctx.drawBorder(x, y, w, h, scaleAlpha(SEARCH_BORDER, alpha));
    }

    private void drawActionButton(DrawContext ctx, int mouseX, int mouseY, int x, int y, int w, int h, String label, float alpha) {
        boolean hover = inside(mouseX, mouseY, x, y, w, h);
        ctx.fill(x, y, x + w, y + h, scaleAlpha(hover ? ACTION_BTN_HOVER : ACTION_BTN, alpha));
        ctx.drawBorder(x, y, w, h, scaleAlpha(0xFF4A5D86, alpha));
        ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(label), x + w / 2, y + 6, scaleAlpha(TEXT, alpha));
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
