package com.saltclient.gui;

import com.saltclient.auth.ElyAuthService;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.concurrent.CompletableFuture;

public final class ElyLoginScreen extends Screen {
    private final Screen parent;

    private TextFieldWidget userField;
    private TextFieldWidget passField;
    private TextFieldWidget urlField;

    private String status = "";
    private int statusColor = 0xFFA0AEC0;
    private boolean busy;

    public ElyLoginScreen(Screen parent) {
        super(Text.literal("Ely.by Login"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int top = this.height / 4;
        int fieldW = 260;
        int fieldH = 20;
        int gap = 10;

        userField = new TextFieldWidget(this.textRenderer, centerX - fieldW / 2, top, fieldW, fieldH, Text.literal("Username / Email"));
        userField.setPlaceholder(Text.literal("Ely.by username / email"));
        this.addSelectableChild(userField);

        passField = new TextFieldWidget(this.textRenderer, centerX - fieldW / 2, top + fieldH + gap, fieldW, fieldH, Text.literal("Password"));
        passField.setPlaceholder(Text.literal("Password"));
        passField.setTextPredicate(s -> true);
        passField.setRenderTextProvider((text, firstCharacterIndex) -> Text.literal("*".repeat(text.length())).asOrderedText());
        this.addSelectableChild(passField);

        urlField = new TextFieldWidget(this.textRenderer, centerX - fieldW / 2, top + (fieldH + gap) * 2, fieldW, fieldH, Text.literal("Auth URL"));
        urlField.setText("https://authserver.ely.by");
        this.addSelectableChild(urlField);

        int btnW = 120;
        int btnH = 20;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Login"), b -> tryLogin())
            .dimensions(centerX - btnW - 6, top + (fieldH + gap) * 3 + 6, btnW, btnH).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(centerX + 6, top + (fieldH + gap) * 3 + 6, btnW, btnH).build());

        setInitialFocus(userField);
    }

    private void tryLogin() {
        if (busy) return;
        busy = true;
        status = "Logging in…";
        statusColor = 0xFF93C5FD;

        String user = userField.getText();
        String pass = passField.getText();
        String url = urlField.getText();

        CompletableFuture<ElyAuthService.Result> fut = ElyAuthService.loginAsync(user, pass, url);
        fut.thenAccept(res -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.execute(() -> {
                busy = false;
                status = res.message();
                statusColor = res.ok() ? 0xFF86EFAC : 0xFFFCA5A5;
            });
        });
    }

    @Override
    public void close() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null) mc.setScreen(parent);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !busy;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int y = userField.getY() - 22;
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, y, 0xFFFFFFFF);

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Username / Email"), userField.getX(), userField.getY() - 12, 0xFFCBD5E1);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Password"), passField.getX(), passField.getY() - 12, 0xFFCBD5E1);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Auth server (keep default for Ely.by)"), urlField.getX(), urlField.getY() - 12, 0xFFCBD5E1);

        if (!status.isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal(status), centerX, urlField.getY() + 52, statusColor);
        }

        if (busy) {
            ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Working…"), centerX, urlField.getY() + 68, 0xFF93C5FD);
        }

        userField.render(ctx, mouseX, mouseY, delta);
        passField.render(ctx, mouseX, mouseY, delta);
        urlField.render(ctx, mouseX, mouseY, delta);
    }
}
