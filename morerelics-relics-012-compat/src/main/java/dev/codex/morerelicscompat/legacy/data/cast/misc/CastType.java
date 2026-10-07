package dev.codex.morerelicscompat.legacy.data.cast.misc;

import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationType;

public enum CastType {
    INSTANTANEOUS,
    INTERRUPTIBLE,
    TOGGLEABLE;

    public AbilityActivationType toCurrent() {
        return AbilityActivationType.valueOf(name());
    }
}

