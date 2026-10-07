package dev.codex.morerelicscompat.legacy.data.leveling;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilitiesTemplate;
import java.util.ArrayList;
import java.util.List;

public final class AbilitiesData {
    private final List<AbilityData> abilities;

    private AbilitiesData(List<AbilityData> abilities) {
        this.abilities = List.copyOf(abilities);
    }

    public static AbilitiesDataBuilder builder() {
        return new AbilitiesDataBuilder();
    }

    public AbilitiesTemplate toTemplate() {
        var builder = AbilitiesTemplate.builder();
        for (AbilityData ability : abilities) {
            builder.ability(ability.toTemplate());
        }
        return builder.build();
    }

    public static final class AbilitiesDataBuilder {
        private final List<AbilityData> abilities = new ArrayList<>();

        public AbilitiesDataBuilder ability(AbilityData ability) {
            if (ability != null) {
                abilities.add(ability);
            }
            return this;
        }

        public AbilitiesData build() {
            return new AbilitiesData(abilities);
        }
    }
}

