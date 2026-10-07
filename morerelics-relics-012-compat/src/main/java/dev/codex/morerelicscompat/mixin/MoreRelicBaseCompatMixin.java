package dev.codex.morerelicscompat.mixin;

import dev.codex.morerelicscompat.legacy.LegacyAbilityComponent;
import dev.codex.morerelicscompat.legacy.LegacyRelicItem;
import dev.codex.morerelicscompat.legacy.data.RelicData;
import dev.codex.morerelicscompat.legacy.data.cast.misc.CastStage;
import dev.codex.morerelicscompat.legacy.data.cast.misc.CastType;
import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationContext;
import java.lang.reflect.InvocationTargetException;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(targets = "com.blorb.morerelics.relics.MoreRelicBase", remap = false)
public abstract class MoreRelicBaseCompatMixin implements LegacyRelicItem {
    public void activateAbility(AbilityActivationContext context) {
        // Relics 0.12 calls this entry point after checking unlocks, predicates and cooldowns.
        // Dispatch to the remapped override on the actual More Relics item.
        ((LegacyRelicItem) (Object) this).castActiveAbility(
            context.stack(), context.player(), context.abilityData().getId(),
            CastType.valueOf(context.type().name()), CastStage.valueOf(context.stage().name())
        );
    }

    public RelicTemplate constructDefaultRelicTemplate() {
        try {
            Object legacyData = getClass().getMethod("constructDefaultRelicData").invoke(this);
            if (legacyData instanceof RelicData relicData) {
                return relicData.toTemplate();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            System.err.println(
                "[MoreRelicsCompat] Failed to translate legacy relic data for " + getClass().getName()
            );
            exception.printStackTrace();
        }

        return RelicTemplate.builder().build();
    }

    public double getStatValue(ItemStack stack, String abilityId, String statId) {
        var stat = ability(stack, null, abilityId);
        return stat == null ? 0.0D : stat.getStatData(statId).getValue();
    }

    public boolean isAbilityUnlocked(ItemStack stack, String abilityId) {
        var ability = ability(stack, null, abilityId);
        return ability != null && ability.isUnlocked();
    }

    public boolean canPlayerUseAbility(Player player, ItemStack stack, String abilityId) {
        var ability = ability(stack, player, abilityId);
        return ability != null && ability.canPlayerUse(player);
    }

    public int getAbilityCooldown(ItemStack stack, String abilityId) {
        var ability = ability(stack, null, abilityId);
        return ability == null ? 0 : ability.getActivationCooldown();
    }

    public void setAbilityCooldown(ItemStack stack, String abilityId, int cooldown) {
        var ability = ability(stack, null, abilityId);
        if (ability != null) {
            ability.setActivationCooldown(Math.max(0, cooldown));
        }
    }

    public boolean isAbilityOnCooldown(ItemStack stack, String abilityId) {
        var ability = ability(stack, null, abilityId);
        return ability != null && ability.isActivationOnCooldown();
    }

    public boolean isAbilityTicking(ItemStack stack, String abilityId) {
        var ability = ability(stack, null, abilityId);
        return ability != null && ability.isActivationTicking();
    }

    public boolean addRelicExperience(LivingEntity entity, ItemStack stack, int amount) {
        return currentData(stack, entity).getLevelingData().addExperience(Math.max(0, amount));
    }

    public void spreadRelicExperience(LivingEntity entity, ItemStack stack, int amount) {
        addRelicExperience(entity, stack, amount);
    }

    public LegacyAbilityComponent getAbilityComponent(ItemStack stack, String abilityId) {
        var ability = ability(stack, null, abilityId);
        return new LegacyAbilityComponent(ability == null ? null : ability.getComponent());
    }

    private it.hurts.sskirillss.relics.api.relics.data.AbilityData ability(
        ItemStack stack,
        LivingEntity entity,
        String abilityId
    ) {
        try {
            return currentData(stack, entity).getAbilitiesData().getAbilityData(abilityId);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private it.hurts.sskirillss.relics.api.relics.data.RelicData currentData(
        ItemStack stack,
        LivingEntity entity
    ) {
        return ((IRelicItem) (Object) this).getRelicData(entity, stack);
    }
}
