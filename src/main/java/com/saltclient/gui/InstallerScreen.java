package com.saltclient.gui;

import com.saltclient.installer.ContentInstaller;
import com.saltclient.installer.InstallType;
import com.saltclient.util.UiFonts;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class InstallerScreen extends Screen {
    private static final int BG = 0xD90B1018;
    private static final int PANEL = 0xE0131A27;
    private static final int PANEL_BORDER = 0xFF23324A;
    private static final int TEXT = 0xFFE6EBFA;
    private static final int MUTED = 0xFF8EA1C8;
    private static final int POSITIVE = 0xFF8DE39F;
    private static final int NEGATIVE = 0xFFFF8A8A;

    private final Screen parent;

    private TextFieldWidget sourceField;
    private ButtonWidget typeButton;
    private ButtonWidget installButton;

    private InstallType type = InstallType.MOD;
    private boolean installing;

    private String status = "Paste a GitHub/Modrinth link or slug";
    private int statusColor = MUTED;

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
        int panelH = 220;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        this.sourceField = new TextFieldWidget(this.textRenderer, panelX + 20, panelY + 70, panelW - 40, 20, UiFonts.text("URL or slug"));
        this.sourceField.setMaxLength(280);
        this.sourceField.setText("https://modrinth.com/mod/");
        addDrawableChild(this.sourceField);

        this.typeButton = addDrawableChild(ButtonWidget.builder(typeText(), b -> {
            type = switch (type) {
                case MOD -> InstallType.TEXTURE_PACK;
                case TEXTURE_PACK -> InstallType.WORLD;
                case WORLD -> InstallType.MOD;
            };
            b.setMessage(typeText());
            setStatus("Destination: " + ContentInstaller.destination(type), MUTED);
        }).dimensions(panelX + 20, panelY + 100, 130, 20).build());

        this.installButton = addDrawableChild(ButtonWidget.builder(UiFonts.text("Install"), b -> startInstall())
            .dimensions(panelX + 160, panelY + 100, 100, 20)
            .build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Refresh"), b -> {
            setStatus("Ready", MUTED);
        }).dimensions(panelX + 270, panelY + 100, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(UiFonts.text("Back"), b -> close())
            .dimensions(panelX + panelW - 120, panelY + panelH - 30, 100, 20)
            .build());

        setInitialFocus(sourceField);
        setStatus("Destination: " + ContentInstaller.destination(type), MUTED);
    }

    private Text typeText() {
        return UiFonts.text("Type: " + type.label);
    }

    private void startInstall() {
        if (installing) return;
        if (sourceField == null) return;

        String input = sourceField.getText();
        if (input == null || input.isBlank()) {
            setStatus("Please enter source link/slug", NEGATIVE);
            return;
        }

        installing = true;
        updateInstallButton();
        setStatus("Starting install...", MUTED);

        ContentInstaller.installAsync(input, type,
            progress -> runOnClient(() -> setStatus(progress, MUTED)),
            (ok, message) -> runOnClient(() -> {
                installing = false;
                updateInstallButton();
                setStatus(message, ok ? POSITIVE : NEGATIVE);
            })
        );
    }

    private void updateInstallButton() {
        if (installButton == null) return;
        installButton.active = !installing;
        installButton.setMessage(UiFonts.text(installing ? "Installing..." : "Install"));
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
        int panelH = 220;
        int panelX = (this.width - panelW) / 2;
        int panelY = (this.height - panelH) / 2;

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL);
        ctx.drawBorder(panelX, panelY, panelW, panelH, PANEL_BORDER);

        ctx.drawCenteredTextWithShadow(this.textRenderer, UiFonts.text("Installers"), this.width / 2, panelY + 14, TEXT);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Supports direct URLs, GitHub repos/releases, and Modrinth links/slugs."), panelX + 20, panelY + 36, MUTED);
        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("Mods -> mods | Texture packs -> resourcepacks | Worlds -> saves"), panelX + 20, panelY + 50, MUTED);

        ctx.drawTextWithShadow(this.textRenderer, UiFonts.text(status), panelX + 20, panelY + panelH - 42, statusColor);

        super.render(ctx, mouseX, mouseY, delta);
    }
}
