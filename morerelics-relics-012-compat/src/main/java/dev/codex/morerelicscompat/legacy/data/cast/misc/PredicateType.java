package dev.codex.morerelicscompat.legacy.data.cast.misc;

import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationPredicateType;

public enum PredicateType {
    CAST,
    VISIBILITY;

    public AbilityActivationPredicateType toCurrent() {
        return AbilityActivationPredicateType.valueOf(name());
    }
}

