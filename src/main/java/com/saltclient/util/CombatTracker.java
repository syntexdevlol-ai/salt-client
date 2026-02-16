package com.saltclient.util;

import com.saltclient.SaltClient;
import com.saltclient.state.SaltState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Client-side combat feedback used by HitColor / HitSound / KillSound.
 *
 * Note: this is best-effort and not authoritative (client doesn't know server hit confirmations).
 */
public final class CombatTracker {
    private static boolean prevAttack;
    private static boolean prevDead;

    private static int lastAttackedEntityId = -1;
    private static long lastAttackedMs;
    private static ClientWorld lastWorld;

    private CombatTracker() {}

    public static void tick(MinecraftClient mc) {
        if (mc == null || mc.player == null || mc.world == null) {
            prevAttack = false;
            prevDead = false;
            lastAttackedEntityId = -1;
            lastWorld = null;
            return;
        }

        if (mc.world != lastWorld) {
            lastWorld = mc.world;
            SaltState.resetMatchStats(System.currentTimeMillis());
        }

        boolean dead = mc.player.isDead() || mc.player.getHealth() <= 0.0f;
        if (dead && !prevDead) {
            SaltState.deathCount++;
            SaltState.streakCount = 0;
            SaltState.lastDeathMs = System.currentTimeMillis();
        }
        prevDead = dead;

        boolean attack = mc.options.attackKey.isPressed();
        if (attack && !prevAttack) {
            onAttackPress(mc);
        }
        prevAttack = attack;

        // Kill detection (best effort)
        if (lastAttackedEntityId != -1 && (System.currentTimeMillis() - lastAttackedMs) <= 5000L) {
            Entity e = mc.world.getEntityById(lastAttackedEntityId);
            boolean targetDead = (e == null) || (e instanceof LivingEntity le && le.isDead());
            if (targetDead) {
                SaltState.killCount++;
                SaltState.streakCount++;
                if (SaltState.streakCount > SaltState.bestStreak) {
                    SaltState.bestStreak = SaltState.streakCount;
                }

                if (SaltClient.MODULES.isEnabled("killsound")) {
                    mc.player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 0.35f, 1.8f);
                    SaltState.lastKillMs = System.currentTimeMillis();
                }
                lastAttackedEntityId = -1;
            }
        }
    }

    private static void onAttackPress(MinecraftClient mc) {
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        Entity target = ehr.getEntity();

        long now = System.currentTimeMillis();
        SaltState.lastHitMs = now;

        if (SaltClient.MODULES.isEnabled("hitsound")) {
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.25f, 1.4f);
        }

        lastAttackedEntityId = target.getId();
        lastAttackedMs = now;
    }
}
