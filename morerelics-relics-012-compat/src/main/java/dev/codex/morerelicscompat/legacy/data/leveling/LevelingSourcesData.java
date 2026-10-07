package dev.codex.morerelicscompat.legacy.data.leveling;

import java.util.ArrayList;
import java.util.List;

public final class LevelingSourcesData {
    private final List<LevelingSourceData> sources;

    private LevelingSourcesData(List<LevelingSourceData> sources) {
        this.sources = List.copyOf(sources);
    }

    public static LevelingSourcesDataBuilder builder() {
        return new LevelingSourcesDataBuilder();
    }

    public List<LevelingSourceData> getSources() {
        return sources;
    }

    public static final class LevelingSourcesDataBuilder {
        private final List<LevelingSourceData> sources = new ArrayList<>();

        public LevelingSourcesDataBuilder source(LevelingSourceData source) {
            if (source != null) {
                sources.add(source);
            }
            return this;
        }

        public LevelingSourcesData build() {
            return new LevelingSourcesData(sources);
        }
    }
}

