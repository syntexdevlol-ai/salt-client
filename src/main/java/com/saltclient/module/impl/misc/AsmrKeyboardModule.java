package com.saltclient.module.impl.misc;

import com.saltclient.module.Module;
import com.saltclient.module.ModuleCategory;
import com.saltclient.setting.BoolSetting;
import com.saltclient.setting.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

public final class AsmrKeyboardModule extends Module {
    private final BoolSetting leftClick = addSetting(new BoolSetting("leftClick", "Left Click", "Play sound on left mouse click.", true));
    private final BoolSetting rightClick = addSetting(new BoolSetting("rightClick", "Right Click", "Play sound on right mouse click.", true));

    private final BoolSetting keyW = addSetting(new BoolSetting("keyW", "Key W", "Play sound when W is pressed.", true));
    private final BoolSetting keyA = addSetting(new BoolSetting("keyA", "Key A", "Play sound when A is pressed.", true));
    private final BoolSetting keyS = addSetting(new BoolSetting("keyS", "Key S", "Play sound when S is pressed.", true));
    private final BoolSetting keyD = addSetting(new BoolSetting("keyD", "Key D", "Play sound when D is pressed.", true));
    private final BoolSetting keySpace = addSetting(new BoolSetting("keySpace", "Space", "Play sound when SPACE is pressed.", true));
    private final BoolSetting keyShift = addSetting(new BoolSetting("keyShift", "Shift", "Play sound when SHIFT is pressed.", false));

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

        mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), vol, realPitch));
    }

    private static boolean isKeyDown(long handle, int key) {
        return GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
    }
}
