package com.saltclient.gui;

import com.saltclient.installer.ContentInstaller;
import com.saltclient.installer.InstallType;
import com.saltclient.installer.ModrinthApi;
import com.saltclient.util.UiDraw;
import com.saltclient.util.UiFonts;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class InstallerScreen extends Screen {
    private static final int BG = 0x7F0B1220;
    private static final int PANEL = 0xF2FFFFFF;
    private static final int PANEL_BORDER = 0xFFCBD5E1;
    private static final int TEXT = 0xFF0F172A;
    private static final int MUTED = 0xFF475569;
    private static final int POSITIVE = 0xFF16A34A;
    private static final int NEGATIVE = 0xFFDC2626;

    private static final int R_PANEL = 14;
    private static final int R_CARD = 12;
    private static final int R_BTN = 10;

    private final Screen parent;

    private TextFieldWidget sourceField;
    private ButtonWidget typeButton;
    private ButtonWidget searchButton;
    private ButtonWidget installButton;

    private InstallType type = InstallType.MOD;
    private boolean installing;
    private boolean searching;

    private String status = "Paste a GitHub/Modrinth link or slug";
    private int statusColor = MUTED;

    private final List<ModrinthApi.Project> results = new ArrayList<>();
    private int selected = -1;
    private double scroll;
    private long lastClickMs;
    private int lastClickIndex = -1;

    public InstallerScreen(Screen parent) {
        super(UiFonts.text("Installers"));
        this.parent = parent;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Skip default blur.
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 40, 620);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        this.sourceField = new TextFieldWidget(this.textRenderer, panelX + 20, panelY + 70, panelW - 40, 20, UiFonts.text("URL or slug"));
        this.sourceField.setMaxLength(280);
        this.sourceField.setText("");
        addDrawableChild(this.sourceField);

        this.typeButton = addDrawableChild(ButtonWidget.builder(typeText(), b -> {
            type = switch (type) {
                case MOD -> InstallType.TEXTURE_PACK;
                case TEXTURE_PACK -> InstallType.WORLD;
                case WORLD -> InstallType.MOD;
            };
            b.setMessage(typeText());
            results.clear();
            selected = -1;
            scroll = 0.0;
            setStatus("Type set to: " + type.label, MUTED);
        }).dimensions(panelX + 20, panelY + 100, 130, 20).build());

        this.searchButton = addDrawableChild(ButtonWidget.builder(UiFonts.text("Search"), b -> startSearch())
            .dimensions(panelX + 160, panelY + 100, 90, 20)
            .build());

        this.installButton = addDrawableChild(ButtonWidget.builder(UiFonts.text("Install"), b -> installOrDirect())
            .dimensions(panelX + 160, panelY + 100, 100, 20)
            .build());

        this.installButton.setX(panelX + 260);
        this.installButton.setY(panelY + 100);
        this.installButton.setWidth(90);
        this.installButton.setHeight(20);

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Clear"), b -> {
            if (sourceField != null) sourceField.setText("");
            results.clear();
            selected = -1;
            scroll = 0.0;
            setStatus("Cleared", MUTED);
        }).dimensions(panelX + 360, panelY + 100, 80, 20).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Back"), b -> close())
            .dimensions(panelX + panelW - 120, panelY + panelH - 30, 100, 20)
            .build());

        setInitialFocus(sourceField);
        setStatus("Search Modrinth or paste a URL/slug (Mods/Texture Packs/Worlds)", MUTED);
    }

    private Text typeText() {
        return UiFonts.text("Type: " + type.label);
    }

    private void startSearch() {
        if (searching || installing) return;
        if (sourceField == null) return;

        String q = sourceField.getText();
        if (q == null || q.isBlank()) {
            setStatus("Enter a search query first", NEGATIVE);
            return;
        }

        searching = true;
        updateButtons();
        setStatus("Searching Modrinth...", MUTED);

        new Thread(() -> {
            try {
                List<ModrinthApi.Project> found = ModrinthApi.search(q, type, 30);
                runOnClient(() -> {
                    searching = false;
                    updateButtons();
                    results.clear();
                    results.addAll(found);
                    selected = results.isEmpty() ? -1 : 0;
                    scroll = 0.0;
                    if (results.isEmpty()) {
                        setStatus("No results", NEGATIVE);
                    } else {
                        setStatus("Found " + results.size() + " result(s). Select one and press Install.", POSITIVE);
                    }
                });
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
                final String finalMsg = msg;
                runOnClient(() -> {
                    searching = false;
                    updateButtons();
                    setStatus(finalMsg, NEGATIVE);
                });
            }
        }, "salt-modrinth-search").start();
    }

    private void installOrDirect() {
        if (installing || searching) return;
        if (selected >= 0 && selected < results.size()) {
            installSelected();
            return;
        }

        startDirectInstall();
    }

    private void installSelected() {
        if (installing || searching) return;
        if (selected < 0 || selected >= results.size()) {
            setStatus("Select a result first", NEGATIVE);
            return;
        }

        ModrinthApi.Project p = results.get(selected);
        installing = true;
        updateButtons();
        setStatus("Resolving download...", MUTED);

        new Thread(() -> {
            try {
                String gv = currentMinecraftVersion();
                ModrinthApi.Asset asset = ModrinthApi.resolveLatestAsset(p.id(), type, gv);
                if (asset == null || asset.url() == null || asset.url().isBlank()) {
                    throw new IOException("No compatible download found for " + gv);
                }

                runOnClient(() -> setStatus("Downloading " + asset.filename() + "...", MUTED));
                ContentInstaller.installAsync(asset.url(), type,
                    progress -> runOnClient(() -> setStatus(progress, MUTED)),
                    (ok, message) -> runOnClient(() -> {
                        installing = false;
                        updateButtons();
                        setStatus(message, ok ? POSITIVE : NEGATIVE);
                    })
                );
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg == null || msg.isBlank()) msg = e.getClass().getSimpleName();
                final String finalMsg = msg;
                runOnClient(() -> {
                    installing = false;
                    updateButtons();
                    setStatus(finalMsg, NEGATIVE);
                });
            }
        }, "salt-modrinth-install").start();
    }

    private void startDirectInstall() {
        if (installing || searching) return;
        if (sourceField == null) return;

        String input = sourceField.getText();
        if (input == null || input.isBlank()) {
            setStatus("Please enter source link/slug", NEGATIVE);
            return;
        }

        installing = true;
        updateButtons();
        setStatus("Starting install...", MUTED);

        ContentInstaller.installAsync(input, type,
            progress -> runOnClient(() -> setStatus(progress, MUTED)),
            (ok, message) -> runOnClient(() -> {
                installing = false;
                updateButtons();
                setStatus(message, ok ? POSITIVE : NEGATIVE);
            })
        );
    }

    private void updateButtons() {
        if (typeButton != null) typeButton.active = !(installing || searching);
        if (searchButton != null) {
            searchButton.active = !(installing || searching);
            searchButton.setMessage(UiFonts.text(searching ? "Searching..." : "Search"));
        }
        if (installButton != null) {
            installButton.active = !(installing || searching);
            installButton.setMessage(UiFonts.text(installing ? "Installing..." : "Install"));
        }
    }

    private void runOnClient(Runnable task) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) {
            mc.execute(task);
        } else {
            task.run();
        }
    }

    private void setStatus(String value, int color) {
        this.status = value == null ? "" : value;
        this.statusColor = color;
    }

    private static String currentMinecraftVersion() {
        try {
            return FabricLoader.getInstance()
                .getModContainer("minecraft")
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        } catch (Exception ignored) {
            return "unknown";
        }
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
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 620);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        UiDraw.panelRounded(ctx, panelX, panelY, panelW, panelH, R_PANEL, PANEL, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text("Installers"), this.width / 2, panelY + 14, TEXT);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Search Modrinth in-game, or paste a direct URL/slug."), panelX + 20, panelY + 36, MUTED);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Installs into: " + ContentInstaller.destination(type)), panelX + 20, panelY + 50, MUTED);

        int listX = panelX + 20;
        int listY = panelY + 130;
        int listW = panelW - 40;
        int listH = panelH - 210;
        if (listH < 80) listH = 80;

        UiDraw.panelRounded(ctx, listX, listY, listW, listH, R_CARD, 0xFFF8FAFC, PANEL_BORDER);

        int rowH = 26;
        int visibleRows = Math.max(1, listH / rowH);
        int maxScroll = Math.max(0, results.size() * rowH - listH);
        if (scroll > maxScroll) scroll = maxScroll;
        if (scroll < 0.0) scroll = 0.0;

        int startIndex = (int) (scroll / rowH);
        int yOffset = (int) (scroll % rowH);

        for (int row = 0; row <= visibleRows; row++) {
            int idx = startIndex + row;
            if (idx >= results.size()) break;

            int y = listY + row * rowH - yOffset;
            if (y + rowH <= listY || y >= listY + listH) continue;

            ModrinthApi.Project p = results.get(idx);
            boolean hover = inside(mouseX, mouseY, listX + 2, y, listW - 4, rowH);
            boolean selectedRow = idx == selected;

            int rowColor;
            if (selectedRow) rowColor = 0x260B63F6;
            else rowColor = hover ? 0x140F172A : 0x00000000;

            UiDraw.fillRounded(ctx, listX + 2, y, listX + listW - 2, y + rowH, R_BTN, rowColor);

            String title = p.title();
            String desc = p.description();

            int tx = listX + 10;
            ctx.drawTextWithShadow(this.textRenderer, Text.literal(title), tx, y + 6, TEXT);
            if (desc != null && !desc.isBlank()) {
                int titleW = this.textRenderer.getWidth(title);
                int descX = tx + titleW + 10;
                int avail = (listX + listW - 10) - descX;
                if (avail > 20) {
                    String shortDesc = trimToWidth(desc, avail);
                    ctx.drawTextWithShadow(this.textRenderer, Text.literal(shortDesc), descX, y + 6, MUTED);
                }
            }
        }

        if (results.isEmpty()) {
            String hint = searching ? "Searching..." : "No results yet. Type a query and press Search.";
            ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text(hint), listX + listW / 2, listY + listH / 2 - 4, MUTED);
        }

        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text(status), panelX + 20, panelY + panelH - 42, statusColor);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let widgets (text field/buttons) handle focus first.
        if (super.mouseClicked(mouseX, mouseY, button)) return true;

        int panelW = Math.min(this.width - 40, 620);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int listX = panelX + 20;
        int listY = panelY + 130;
        int listW = panelW - 40;
        int listH = panelH - 210;
        if (listH < 80) listH = 80;

        if (inside(mouseX, mouseY, listX, listY, listW, listH)) {
            int rowH = 26;
            int idx = (int) ((mouseY - listY + scroll) / rowH);
            if (idx >= 0 && idx < results.size()) {
                selected = idx;
                long now = System.currentTimeMillis();
                if (idx == lastClickIndex && now - lastClickMs <= 320L) {
                    installSelected();
                }
                lastClickIndex = idx;
                lastClickMs = now;
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int panelW = Math.min(this.width - 40, 620);
        int panelH = Math.min(this.height - 40, 460);
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        int listX = panelX + 20;
        int listY = panelY + 130;
        int listW = panelW - 40;
        int listH = panelH - 210;
        if (listH < 80) listH = 80;

        if (inside(mouseX, mouseY, listX, listY, listW, listH)) {
            int rowH = 26;
            int maxScroll = Math.max(0, results.size() * rowH - listH);
            scroll -= verticalAmount * 26.0;
            if (scroll < 0.0) scroll = 0.0;
            if (scroll > maxScroll) scroll = maxScroll;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
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
}
