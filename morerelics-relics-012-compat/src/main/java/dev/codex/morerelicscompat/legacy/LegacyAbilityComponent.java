package dev.codex.morerelicscompat.legacy;

import it.hurts.sskirillss.relics.api.relics.abilities.AbilityComponent;

/** Minimal wrapper for the one legacy AbilityComponent call made by More Relics. */
public final class LegacyAbilityComponent {
    private final AbilityComponent delegate;

    public LegacyAbilityComponent(AbilityComponent delegate) {
        this.delegate = delegate;
    }

    public AbilityComponent getDelegate() {
        return delegate;
    }

    public AbilityComponentBuilder toBuilder() {
        return new AbilityComponentBuilder(delegate);
    }

    public static final class AbilityComponentBuilder {
        private final AbilityComponent delegate;

        private AbilityComponentBuilder(AbilityComponent delegate) {
            this.delegate = delegate;
        }

        public LegacyAbilityComponent build() {
            return new LegacyAbilityComponent(delegate);
        }
    }
}

