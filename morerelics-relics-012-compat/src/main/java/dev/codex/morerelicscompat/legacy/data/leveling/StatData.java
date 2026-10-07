package dev.codex.morerelicscompat.legacy.data.leveling;

import dev.codex.morerelicscompat.legacy.data.leveling.misc.UpgradeOperation;
import it.hurts.sskirillss.relics.api.relics.abilities.stats.AbilityStatTemplate;
import it.hurts.sskirillss.relics.init.RelicsScalingModels;
import java.util.function.Function;

public final class StatData {
    private final String id;
    private final double initialMin;
    private final double initialMax;
    private final UpgradeOperation operation;
    private final double upgradeModifier;
    private final Function<Double, ? extends Number> formatter;

    private StatData(
        String id,
        double initialMin,
        double initialMax,
        UpgradeOperation operation,
        double upgradeModifier,
        Function<Double, ? extends Number> formatter
    ) {
        this.id = id;
        this.initialMin = initialMin;
        this.initialMax = initialMax;
        this.operation = operation;
        this.upgradeModifier = upgradeModifier;
        this.formatter = formatter;
    }

    public static StatDataBuilder builder(String id) {
        return new StatDataBuilder(id);
    }

    public AbilityStatTemplate toTemplate(int maxLevel) {
        var builder = AbilityStatTemplate.builder(id).initialValue(initialMin, initialMax);

        if (operation == UpgradeOperation.ADD && upgradeModifier != 0.0D) {
            double target = initialMax + upgradeModifier * Math.max(1, maxLevel);
            builder.targetValue(RelicsScalingModels.ADDITIVE.get(), target);
        }

        if (formatter != null) {
            builder.formatValue(formatter);
        }

        return builder.build();
    }

    public static final class StatDataBuilder {
        private final String id;
        private double initialMin;
        private double initialMax;
        private UpgradeOperation operation = UpgradeOperation.ADD;
        private double upgradeModifier;
        private Function<Double, ? extends Number> formatter;

        private StatDataBuilder(String id) {
            this.id = id;
        }

        public StatDataBuilder initialValue(double min, double max) {
            this.initialMin = min;
            this.initialMax = max;
            return this;
        }

        public StatDataBuilder upgradeModifier(UpgradeOperation operation, double modifier) {
            this.operation = operation;
            this.upgradeModifier = modifier;
            return this;
        }

        public StatDataBuilder formatValue(Function<Double, ? extends Number> formatter) {
            this.formatter = formatter;
            return this;
        }

        public StatData build() {
            return new StatData(id, initialMin, initialMax, operation, upgradeModifier, formatter);
        }
    }
}

