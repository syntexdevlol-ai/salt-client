package com.saltclient.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class HudRenderUtil {
    private HudRenderUtil() {}

    public static void textBox(DrawContext ctx, TextRenderer tr, String text, int x, int y, int fg, int bg) {
        int padX = 4;
        int padY = 2;
        int w = tr.getWidth(text);
        int h = tr.fontHeight;
        ctx.fill(x - padX, y - padY, x + w + padX, y + h + padY, bg);
        ctx.drawTextWithShadow(tr, Text.literal(text), x, y, fg);
    }
}

