package com.saltclient.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class HudRenderUtil {
    private HudRenderUtil() {}

    public static void textBox(DrawContext ctx, TextRenderer tr, String text, int x, int y, int fg, int bg) {
        int padX = 4;
        int padY = 2;
        int w = tr.getWidth(text);
        int h = tr.fontHeight;
        ctx.fill(x - padX, y - padY, x + w + padX, y + h + padY, bg);
        ctx.drawTextWithShadow(tr, UiFonts.text(text), x, y, fg);
    }

    public static void textBoxTL(DrawContext ctx, TextRenderer tr, String text, int x, int y, int fg, int bg) {
        int padX = 4;
        int padY = 2;
        int w = tr.getWidth(text);
        int h = tr.fontHeight;
        ctx.fill(x, y, x + w + padX * 2, y + h + padY * 2, bg);
        ctx.drawTextWithShadow(tr, UiFonts.text(text), x + padX, y + padY, fg);
    }

    public static void textBoxHud(DrawContext ctx, TextRenderer tr, String id, String text, int defaultX, int defaultY, int fg, int bg) {
        HudPos.Pos p = HudPos.resolve(id, defaultX, defaultY);
        int x = p.x;
        int y = p.y;

        float scale = HudScale.factor();
        if (scale != 1.0f) {
            ctx.getMatrices().push();
            ctx.getMatrices().translate(x, y, 0);
            ctx.getMatrices().scale(scale, scale, 1.0f);

            int padX = 4;
            int padY = 2;
            int w = tr.getWidth(text) + padX * 2;
            int h = tr.fontHeight + padY * 2;

            textBoxTL(ctx, tr, text, 0, 0, fg, bg);
            HudPos.recordBounds(id, x, y, Math.round(w * scale), Math.round(h * scale));

            ctx.getMatrices().pop();
        } else {
            int padX = 4;
            int padY = 2;
            int w = tr.getWidth(text) + padX * 2;
            int h = tr.fontHeight + padY * 2;

            textBoxTL(ctx, tr, text, x, y, fg, bg);
            HudPos.recordBounds(id, x, y, w, h);
        }
    }
}
