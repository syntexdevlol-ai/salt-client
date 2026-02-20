package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.EnumSetting;
import com.saltclient.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class AsmrKeyboardModule extends Module {
    private enum AsmrSound {
        UI_CLICK("UI Click", SoundEvents.UI_BUTTON_CLICK.value()),
        TRIPWIRE("Tripwire", SoundEvents.BLOCK_TRIPWIRE_CLICK_ON),
        NOTE_HAT("Note Hat", SoundEvents.BLOCK_NOTE_BLOCK_HAT.value()),
        NOTE_SNARE("Note Snare", SoundEvents.BLOCK_NOTE_BLOCK_SNARE.value()),
        NOTE_PLING("Note Pling", SoundEvents.BLOCK_NOTE_BLOCK_PLING.value());

        final String label;
        final SoundEvent event;

        AsmrSound(String label, SoundEvent event) {
            this.label = label;
            this.event = event;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final BoolSetting leftClick = addSetting(new BoolSetting("leftClick", "Left Click", "Play sound on left mouse click.", true));
    private final BoolSetting rightClick = addSetting(new BoolSetting("rightClick", "Right Click", "Play sound on right mouse click.", true));

    private final BoolSetting keyW = addSetting(new BoolSetting("keyW", "Key W", "Play sound when W is pressed.", true));
    private final BoolSetting keyA = addSetting(new BoolSetting("keyA", "Key A", "Play sound when A is pressed.", true));
    private final BoolSetting keyS = addSetting(new BoolSetting("keyS", "Key S", "Play sound when S is pressed.", true));
    private final BoolSetting keyD = addSetting(new BoolSetting("keyD", "Key D", "Play sound when D is pressed.", true));
    private final BoolSetting keySpace = addSetting(new BoolSetting("keySpace", "Space", "Play sound when SPACE is pressed.", true));
    private final BoolSetting keyShift = addSetting(new BoolSetting("keyShift", "Shift", "Play sound when SHIFT is pressed.", false));

    private final BoolSetting keyE = addSetting(new BoolSetting("keyE", "Key E", "Play sound when E is pressed.", false));
    private final BoolSetting keyQ = addSetting(new BoolSetting("keyQ", "Key Q", "Play sound when Q is pressed.", false));
    private final BoolSetting keyR = addSetting(new BoolSetting("keyR", "Key R", "Play sound when R is pressed.", false));
    private final BoolSetting keyF = addSetting(new BoolSetting("keyF", "Key F", "Play sound when F is pressed.", false));

    private final BoolSetting arrowKeys = addSetting(new BoolSetting("arrowKeys", "Arrow Keys", "Play sound when arrow keys are pressed.", false));

    private final BoolSetting hotbarKeys = addSetting(new BoolSetting("hotbarKeys", "Hotbar 1-9", "Play sound when 1-9 are pressed.", false));

    private final EnumSetting<AsmrSound> sound = addSetting(new EnumSetting<>("sound", "Sound", "Which sound to play.", AsmrSound.UI_CLICK, AsmrSound.values()));
    private final IntSetting volume = addSetting(new IntSetting("volume", "Volume", "Keyboard sound volume (%).", 70, 5, 100, 5));
    private final IntSetting pitch = addSetting(new IntSetting("pitch", "Pitch", "Keyboard sound pitch (%).", 95, 60, 150, 5));

    private boolean prevLeft;
    private boolean prevRight;
    private boolean prevW;
    private boolean prevA;
    private boolean prevS;
    private boolean prevD;
    private boolean prevSpace;
    private boolean prevShift;
    private boolean prevE;
    private boolean prevQ;
    private boolean prevR;
    private boolean prevF;
    private boolean prevArUp;
    private boolean prevArDown;
    private boolean prevArLeft;
    private boolean prevArRight;
    private boolean prev1;
    private boolean prev2;
    private boolean prev3;
    private boolean prev4;
    private boolean prev5;
    private boolean prev6;
    private boolean prev7;
    private boolean prev8;
    private boolean prev9;

    private long nextPlayMs;

    public AsmrKeyboardModule() {
        super("asmrkeyboard", "ASMRKeyboard", "Play keyboard/mouse click sounds with per-key toggles.", ModuleCategory.MISC, true);
    }

    @Override
    protected void onDisable(MinecraftClient mc) {
        prevLeft = false;
        prevRight = false;
        prevW = false;
        prevA = false;
        prevS = false;
        prevD = false;
        prevSpace = false;
        prevShift = false;
        prevE = false;
        prevQ = false;
        prevR = false;
        prevF = false;
        prevArUp = false;
        prevArDown = false;
        prevArLeft = false;
        prevArRight = false;
        prev1 = false;
        prev2 = false;
        prev3 = false;
        prev4 = false;
        prev5 = false;
        prev6 = false;
        prev7 = false;
        prev8 = false;
        prev9 = false;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc == null || mc.getWindow() == null) return;
        long handle = mc.getWindow().getHandle();

        prevLeft = handlePress(mc, leftClick.getValue(), GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS, prevLeft, 1.03f);
        prevRight = handlePress(mc, rightClick.getValue(), GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS, prevRight, 0.9f);

        prevW = handlePress(mc, keyW.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_W), prevW, 1.0f);
        prevA = handlePress(mc, keyA.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_A), prevA, 0.98f);
        prevS = handlePress(mc, keyS.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_S), prevS, 0.97f);
        prevD = handlePress(mc, keyD.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_D), prevD, 1.01f);
        prevSpace = handlePress(mc, keySpace.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_SPACE), prevSpace, 0.86f);
        prevShift = handlePress(mc, keyShift.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_LEFT_SHIFT) || isKeyDown(handle, GLFW.GLFW_KEY_RIGHT_SHIFT), prevShift, 0.93f);

        prevE = handlePress(mc, keyE.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_E), prevE, 1.02f);
        prevQ = handlePress(mc, keyQ.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_Q), prevQ, 1.01f);
        prevR = handlePress(mc, keyR.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_R), prevR, 0.99f);
        prevF = handlePress(mc, keyF.getValue(), isKeyDown(handle, GLFW.GLFW_KEY_F), prevF, 1.00f);

        if (arrowKeys.getValue()) {
            prevArUp = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_UP), prevArUp, 0.98f);
            prevArDown = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_DOWN), prevArDown, 0.98f);
            prevArLeft = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_LEFT), prevArLeft, 0.98f);
            prevArRight = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_RIGHT), prevArRight, 0.98f);
        } else {
            prevArUp = isKeyDown(handle, GLFW.GLFW_KEY_UP);
            prevArDown = isKeyDown(handle, GLFW.GLFW_KEY_DOWN);
            prevArLeft = isKeyDown(handle, GLFW.GLFW_KEY_LEFT);
            prevArRight = isKeyDown(handle, GLFW.GLFW_KEY_RIGHT);
        }

        if (hotbarKeys.getValue()) {
            prev1 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_1), prev1, 1.0f);
            prev2 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_2), prev2, 1.0f);
            prev3 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_3), prev3, 1.0f);
            prev4 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_4), prev4, 1.0f);
            prev5 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_5), prev5, 1.0f);
            prev6 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_6), prev6, 1.0f);
            prev7 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_7), prev7, 1.0f);
            prev8 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_8), prev8, 1.0f);
            prev9 = handlePress(mc, true, isKeyDown(handle, GLFW.GLFW_KEY_9), prev9, 1.0f);
        } else {
            prev1 = isKeyDown(handle, GLFW.GLFW_KEY_1);
            prev2 = isKeyDown(handle, GLFW.GLFW_KEY_2);
            prev3 = isKeyDown(handle, GLFW.GLFW_KEY_3);
            prev4 = isKeyDown(handle, GLFW.GLFW_KEY_4);
            prev5 = isKeyDown(handle, GLFW.GLFW_KEY_5);
            prev6 = isKeyDown(handle, GLFW.GLFW_KEY_6);
            prev7 = isKeyDown(handle, GLFW.GLFW_KEY_7);
            prev8 = isKeyDown(handle, GLFW.GLFW_KEY_8);
            prev9 = isKeyDown(handle, GLFW.GLFW_KEY_9);
        }
    }

    private boolean handlePress(MinecraftClient mc, boolean enabled, boolean down, boolean previous, float pitchAdjust) {
        if (enabled && down && !previous) {
            playKeySound(mc, pitchAdjust);
        }
        return down;
    }

    private void playKeySound(MinecraftClient mc, float pitchAdjust) {
        long now = System.currentTimeMillis();
        if (now < nextPlayMs) return;
        nextPlayMs = now + 12L;

        float vol = volume.getValue() / 100.0f;
        float basePitch = pitch.getValue() / 100.0f;
        float realPitch = Math.max(0.5f, Math.min(2.0f, basePitch * pitchAdjust));

        SoundEvent ev = sound.getValue() == null ? SoundEvents.UI_BUTTON_CLICK.value() : sound.getValue().event;
        mc.getSoundManager().play(PositionedSoundInstance.master(ev, vol, realPitch));
    }

    private static boolean isKeyDown(long handle, int key) {
        return GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
    }
}
