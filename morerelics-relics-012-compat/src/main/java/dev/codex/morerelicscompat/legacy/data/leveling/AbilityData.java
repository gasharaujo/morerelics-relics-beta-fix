package dev.codex.morerelicscompat.legacy.data.leveling;

import com.mojang.datafixers.util.Function3;
import dev.codex.morerelicscompat.legacy.data.cast.CastData;
import it.hurts.sskirillss.relics.api.relics.abilities.AbilityTemplate;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AbilityData {
    private final String id;
    private final List<StatData> stats;
    private final int maxLevel;
    private final int requiredLevel;
    private final int requiredPoints;
    private final CastData activation;
    private final Function3<Player, ItemStack, String, String> icon;

    private AbilityData(
        String id,
        List<StatData> stats,
        int maxLevel,
        int requiredLevel,
        int requiredPoints,
        CastData activation,
        Function3<Player, ItemStack, String, String> icon
    ) {
        this.id = id;
        this.stats = List.copyOf(stats);
        this.maxLevel = maxLevel;
        this.requiredLevel = requiredLevel;
        this.requiredPoints = requiredPoints;
        this.activation = activation;
        this.icon = icon;
    }

    public static AbilityDataBuilder builder(String id) {
        return new AbilityDataBuilder(id);
    }

    public AbilityTemplate toTemplate() {
        var builder = AbilityTemplate.builder(id)
            .initialMaxLevel(Math.max(1, maxLevel))
            .requiredLevel(Math.max(0, requiredLevel))
            .requiredPoints(Math.max(1, requiredPoints));

        for (StatData stat : stats) {
            builder.stat(stat.toTemplate(maxLevel));
        }

        if (icon != null) {
            builder.icon(icon);
        }

        if (activation != null) {
            builder.active(activation.toTemplate());
        } else {
            builder.passive();
        }

        return builder.build();
    }

    public static final class AbilityDataBuilder {
        private final String id;
        private final List<StatData> stats = new ArrayList<>();
        private int maxLevel = 10;
        private int requiredLevel;
        private int requiredPoints = 1;
        private CastData activation;
        private Function3<Player, ItemStack, String, String> icon;

        private AbilityDataBuilder(String id) {
            this.id = id;
        }

        public AbilityDataBuilder stat(StatData stat) {
            if (stat != null) {
                stats.add(stat);
            }
            return this;
        }

        public AbilityDataBuilder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public AbilityDataBuilder requiredLevel(int requiredLevel) {
            this.requiredLevel = requiredLevel;
            return this;
        }

        public AbilityDataBuilder requiredPoints(int requiredPoints) {
            this.requiredPoints = requiredPoints;
            return this;
        }

        public AbilityDataBuilder active(CastData activation) {
            this.activation = activation;
            return this;
        }

        public AbilityDataBuilder icon(Function3<Player, ItemStack, String, String> icon) {
            this.icon = icon;
            return this;
        }

        public AbilityData build() {
            return new AbilityData(id, stats, maxLevel, requiredLevel, requiredPoints, activation, icon);
        }
    }
}

