package dev.codex.morerelicscompat.legacy.data;

import dev.codex.morerelicscompat.legacy.data.leveling.AbilitiesData;
import dev.codex.morerelicscompat.legacy.data.leveling.LevelingData;
import dev.codex.morerelicscompat.legacy.data.loot.LootData;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;

/** Legacy RelicData definition translated into the 0.12 RelicTemplate API. */
public final class RelicData {
    private final AbilitiesData abilities;
    private final LevelingData leveling;
    private final LootData loot;

    private RelicData(AbilitiesData abilities, LevelingData leveling, LootData loot) {
        this.abilities = abilities;
        this.leveling = leveling;
        this.loot = loot;
    }

    public static RelicDataBuilder builder() {
        return new RelicDataBuilder();
    }

    public RelicTemplate toTemplate() {
        var builder = RelicTemplate.builder();

        if (abilities != null) {
            builder.abilities(abilities.toTemplate());
        }
        if (leveling != null) {
            builder.leveling(leveling.toTemplate());
        }
        if (loot != null) {
            builder.loot(loot.toTemplate());
        }

        return builder.build();
    }

    public static final class RelicDataBuilder {
        private AbilitiesData abilities;
        private LevelingData leveling;
        private LootData loot;

        public RelicDataBuilder abilities(AbilitiesData abilities) {
            this.abilities = abilities;
            return this;
        }

        public RelicDataBuilder leveling(LevelingData leveling) {
            this.leveling = leveling;
            return this;
        }

        public RelicDataBuilder loot(LootData loot) {
            this.loot = loot;
            return this;
        }

        public RelicData build() {
            return new RelicData(abilities, leveling, loot);
        }
    }
}

