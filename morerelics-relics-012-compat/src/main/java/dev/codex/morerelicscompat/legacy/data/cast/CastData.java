package dev.codex.morerelicscompat.legacy.data.cast;

import dev.codex.morerelicscompat.legacy.data.cast.misc.CastType;
import dev.codex.morerelicscompat.legacy.data.cast.misc.PredicateType;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class CastData {
    private final CastType type;
    private final List<LegacyPredicate> predicates;

    private CastData(CastType type, List<LegacyPredicate> predicates) {
        this.type = type;
        this.predicates = List.copyOf(predicates);
    }

    public static CastDataBuilder builder() {
        return new CastDataBuilder();
    }

    public AbilityActivationTemplate toTemplate() {
        var builder = AbilityActivationTemplate.builder()
            .type((type == null ? CastType.INSTANTANEOUS : type).toCurrent());

        for (LegacyPredicate predicate : predicates) {
            builder.predicate(
                predicate.id(),
                predicate.type().toCurrent(),
                context -> Boolean.TRUE.equals(predicate.test().apply(context.player(), context.stack()))
            );
        }

        return builder.build();
    }

    private record LegacyPredicate(
        String id,
        PredicateType type,
        BiFunction<Player, ItemStack, Boolean> test
    ) {
    }

    public static final class CastDataBuilder {
        private CastType type = CastType.INSTANTANEOUS;
        private final List<LegacyPredicate> predicates = new ArrayList<>();

        public CastDataBuilder type(CastType type) {
            this.type = type;
            return this;
        }

        public CastDataBuilder predicate(
            String id,
            PredicateType type,
            BiFunction<Player, ItemStack, Boolean> test
        ) {
            if (id != null && type != null && test != null) {
                predicates.add(new LegacyPredicate(id, type, test));
            }
            return this;
        }

        public CastData build() {
            return new CastData(type, predicates);
        }
    }
}

