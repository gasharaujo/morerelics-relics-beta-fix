package dev.codex.morerelicscompat.legacy;

import dev.codex.morerelicscompat.legacy.data.cast.misc.CastStage;
import dev.codex.morerelicscompat.legacy.data.cast.misc.CastType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Legacy callbacks implemented by More Relics after its descriptors are remapped. */
public interface LegacyRelicItem extends ICurioItem {
    default void castActiveAbility(ItemStack stack, Player player, String abilityId,
                                   CastType type, CastStage stage) {
        // Passive relics do not implement the old activation callback.
    }
}
