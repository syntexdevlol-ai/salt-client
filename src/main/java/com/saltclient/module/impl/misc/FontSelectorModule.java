package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.EnumSetting;
import net.minecraft.util.Identifier;

public final class FontSelectorModule extends Module {
    public enum UiFont {
        DEFAULT("Default", Identifier.ofVanilla("default")),
        UNIFORM("Uniform", Identifier.ofVanilla("uniform")),
        ALT("Alt", Identifier.ofVanilla("alt"));

        public final String label;
        public final Identifier id;

        UiFont(String label, Identifier id) {
            this.label = label;
            this.id = id;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final EnumSetting<UiFont> font = addSetting(new EnumSetting<>("font", "UI Font", "Font used by Salt GUI and HUD text.", UiFont.DEFAULT, UiFont.values()));

    public FontSelectorModule() {
        super("fontselector", "FontSelector", "Switch between multiple UI font styles.", ModuleCategory.MISC, true);
        setEnabledFromConfig(true);
    }

    public Identifier selectedFont() {
        return font.getValue().id;
    }
}
