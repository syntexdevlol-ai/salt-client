package com.saltclient.gui;

import com.saltclient.chat.ChatMessage;
import com.saltclient.chat.ChatService;
import com.saltclient.util.UiDraw;
import com.saltclient.util.UiFonts;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Very small global chat UI. Uses {@link ChatService} for transport.
 */
public final class GlobalChatScreen extends Screen {
    private static final int BG = 0xC0141728;
    private static final int PANEL = 0xF2182233;
    private static final int BORDER = 0xFF3B82F6;
    private static final int INPUT_BG = 0xFF0F172A;
    private static final int INPUT_BORDER = 0xFF334155;
    private static final int TEXT = 0xFFE2E8F0;
    private static final int MUTED = 0xFF94A3B8;
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    private TextFieldWidget input;
    private int scrollOffset;
    private List<ChatMessage> snapshot = List.of();

    public GlobalChatScreen() {
        super(Text.literal("Global Chat"));
    }

    @Override
    protected void init() {
        ChatService.INSTANCE.ensureConnected();
        ChatService.INSTANCE.setOnUpdate(v -> {
            snapshot = ChatService.INSTANCE.snapshot();
            return null;
        });
        snapshot = ChatService.INSTANCE.snapshot();

        int w = 220;
        input = new TextFieldWidget(this.textRenderer, (this.width - w) / 2, this.height - 40, w, 18, Text.literal("message"));
        addSelectableChild(input);
        setInitialFocus(input);

        addDrawableChild(ButtonWidget.builder(Text.literal("Send"), b -> send())
            .dimensions(input.getX() + input.getWidth() + 8, input.getY(), 60, 18)
            .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Back"), b -> close())
            .dimensions(12, 12, 60, 18)
            .build());
    }

    @Override
    public void close() {
        this.client.setScreen(null);
    }

    private void send() {
        String msg = input.getText();
        ChatService.INSTANCE.send(msg);
        input.setText("");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257 || keyCode == 335) { // Enter
            send();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, BG);

        int panelW = Math.min(this.width - 40, 420);
        int panelH = Math.min(this.height - 80, 260);
        int px = (this.width - panelW) / 2;
        int py = 28;
        UiDraw.panelRounded(ctx, px, py, panelW, panelH, 12, PANEL, BORDER);

        int y = py + 12;
        int lineH = this.textRenderer.fontHeight + 4;
        int visibleLines = (panelH - 24) / lineH;

        List<ChatMessage> list = snapshot;
        int start = Math.max(0, list.size() - visibleLines - scrollOffset);
        int end = Math.min(list.size(), start + visibleLines);

        for (int i = start; i < end; i++) {
            ChatMessage m = list.get(i);
            String ts = TS.format(m.time);
            String line = "[" + ts + "] " + m.user + ": " + m.text;
            ctx.drawTextWithShadow(this.textRenderer, UiFonts.text(line), px + 12, y, TEXT);
            y += lineH;
        }

        if (list.isEmpty()) {
            ctx.drawTextWithShadow(this.textRenderer, UiFonts.text("No messages yet. Say hi!"), px + 12, y, MUTED);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }
}
