package com.saltclient.mixin;

import com.saltclient.gui.SaltScreen;
import com.saltclient.util.GuiSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void salt_addMenuButton(CallbackInfo ci) {
        if (!GuiSettings.customMainMenuEnabled()) return;

        int x = this.width / 2 - 100;
        int y = this.height / 4 + 96 + 72;
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Salt Menu"), button -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) mc.setScreen(new SaltScreen());
        }).dimensions(x, y, 200, 20).build());
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void salt_renderCustomMenuOverlay(DrawContext ctx, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (!GuiSettings.customMainMenuEnabled()) return;

        ctx.fill(0, 0, this.width, this.height, 0x2210182A);

        int x = 12;
        int y = 12;
        int w = 230;
        int h = 60;

        ctx.fill(x, y, x + w, y + h, 0xAA0E1525);
        ctx.drawBorder(x, y, w, h, 0xFF35507A);
        ctx.fill(x, y + h - 3, x + w, y + h, 0xFF4DA0FF);

        ctx.drawTextWithShadow(this.textRenderer, Text.literal("saltclient custom menu"), x + 10, y + 10, 0xFFEAF1FF);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Press " + menuKeyText() + " to open GUI"), x + 10, y + 28, 0xFFAEC3E8);
        ctx.drawTextWithShadow(this.textRenderer, Text.literal("Use Configs tab to save/load JSON"), x + 10, y + 42, 0xFF91A5CF);
    }

    private static String menuKeyText() {
        int key = GuiSettings.menuKey();
        if (key < 0) return "UNBOUND";
        try {
            String text = InputUtil.fromKeyCode(key, GLFW.GLFW_KEY_UNKNOWN).getLocalizedText().getString();
            if (text == null || text.isEmpty()) return "KEY_" + key;
            return text.toUpperCase();
        } catch (Exception ignored) {
            return "KEY_" + key;
        }
    }
}
