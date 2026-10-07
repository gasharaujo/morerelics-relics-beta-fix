package dev.codex.morerelicscompat.legacy;

import it.hurts.sskirillss.relics.init.RelicsMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Binary aliases for EffectRegistry, renamed to RelicsMobEffects in Relics 0.12. */
public final class LegacyEffectRegistry {
    public static final DeferredHolder<MobEffect, MobEffect> STUN = RelicsMobEffects.STUN;
    public static final DeferredHolder<MobEffect, MobEffect> PARALYSIS = RelicsMobEffects.PARALYSIS;
    public static final DeferredHolder<MobEffect, MobEffect> ANTI_HEAL = RelicsMobEffects.ANTI_HEAL;

    private LegacyEffectRegistry() {
    }
}

