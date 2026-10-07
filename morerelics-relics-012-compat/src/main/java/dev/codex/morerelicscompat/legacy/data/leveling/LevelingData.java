package dev.codex.morerelicscompat.legacy.data.leveling;

import it.hurts.sskirillss.relics.items.relics.base.data.leveling.LevelingTemplate;

public final class LevelingData {
    private final int maxLevel;
    private final int initialCost;
    private final int step;
    private final LevelingSourcesData sources;

    private LevelingData(int maxLevel, int initialCost, int step, LevelingSourcesData sources) {
        this.maxLevel = maxLevel;
        this.initialCost = initialCost;
        this.step = step;
        this.sources = sources;
    }

    public static LevelingDataBuilder builder() {
        return new LevelingDataBuilder();
    }

    public LevelingTemplate toTemplate() {
        // Relics 0.12 moved per-ability max levels into AbilityTemplate. The
        // global legacy maxLevel and source list are retained for binary
        // compatibility; experience is bridged through LevelingData directly.
        return LevelingTemplate.builder()
            .initialCost(initialCost)
            .step(step)
            .build();
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public LevelingSourcesData getSources() {
        return sources;
    }

    public static final class LevelingDataBuilder {
        private int maxLevel = 10;
        private int initialCost = 100;
        private int step = 100;
        private LevelingSourcesData sources;

        public LevelingDataBuilder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public LevelingDataBuilder initialCost(int initialCost) {
            this.initialCost = initialCost;
            return this;
        }

        public LevelingDataBuilder step(int step) {
            this.step = step;
            return this;
        }

        public LevelingDataBuilder sources(LevelingSourcesData sources) {
            this.sources = sources;
            return this;
        }

        public LevelingData build() {
            return new LevelingData(maxLevel, initialCost, step, sources);
        }
    }
}

