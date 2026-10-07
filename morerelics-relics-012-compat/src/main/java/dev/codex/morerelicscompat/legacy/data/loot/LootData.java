package dev.codex.morerelicscompat.legacy.data.loot;

import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootEntry;
import it.hurts.sskirillss.relics.items.relics.base.data.loot.LootTemplate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class LootData {
    private final List<LootEntry> entries;

    private LootData(List<LootEntry> entries) {
        this.entries = List.copyOf(entries);
    }

    public static LootDataBuilder builder() {
        return new LootDataBuilder();
    }

    public LootTemplate toTemplate() {
        return LootTemplate.builder().entry(entries.toArray(LootEntry[]::new)).build();
    }

    public static final class LootDataBuilder {
        private final List<LootEntry> entries = new ArrayList<>();

        public LootDataBuilder entry(LootEntry... entries) {
            if (entries != null) {
                this.entries.addAll(Arrays.asList(entries));
            }
            return this;
        }

        public LootData build() {
            return new LootData(entries);
        }
    }
}

