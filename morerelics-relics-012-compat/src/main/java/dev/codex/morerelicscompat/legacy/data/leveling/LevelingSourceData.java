package dev.codex.morerelicscompat.legacy.data.leveling;

public final class LevelingSourceData {
    private final String ability;
    private final int initialValue;

    private LevelingSourceData(String ability, int initialValue) {
        this.ability = ability;
        this.initialValue = initialValue;
    }

    public static LevelingSourceDataBuilder abilityBuilder(String ability) {
        return new LevelingSourceDataBuilder(ability);
    }

    public String getAbility() {
        return ability;
    }

    public int getInitialValue() {
        return initialValue;
    }

    public static final class LevelingSourceDataBuilder {
        private final String ability;
        private int initialValue;

        private LevelingSourceDataBuilder(String ability) {
            this.ability = ability;
        }

        public LevelingSourceDataBuilder initialValue(int initialValue) {
            this.initialValue = initialValue;
            return this;
        }

        public LevelingSourceData build() {
            return new LevelingSourceData(ability, initialValue);
        }
    }
}

