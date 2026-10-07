package dev.codex.morerelicscompat.mixin;

import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import org.spongepowered.asm.mixin.Mixin;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Restores the Curios default-method parent expected by More Relics 1.7.6. */
@Mixin(value = RelicItem.class, remap = false)
public abstract class RelicItemCuriosCompatMixin implements ICurioItem {
}

