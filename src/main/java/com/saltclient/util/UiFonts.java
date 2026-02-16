package com.saltclient.util;

import com.saltclient.SaltClient;
import com.saltclient.module.Module;
import com.saltclient.module.impl.misc.FontSelectorModule;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class UiFonts {
    private UiFonts() {}

    public static Text text(String value) {
        MutableText text = Text.literal(value == null ? "" : value);
        Identifier font = selectedFont();
        if (font == null) return text;
        return text.styled(style -> style.withFont(font));
    }

    public static Identifier selectedFont() {
        Module module = SaltClient.MODULES.byId("fontselector").orElse(null);
        if (module instanceof FontSelectorModule selector) {
            return selector.selectedFont();
        }
        return Identifier.ofVanilla("default");
    }
}
