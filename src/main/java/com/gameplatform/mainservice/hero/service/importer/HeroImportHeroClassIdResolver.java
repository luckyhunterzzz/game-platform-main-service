package com.gameplatform.mainservice.hero.service.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HeroImportHeroClassIdResolver {

    private static final Map<String, Long> HERO_CLASS_ID_BY_SOURCE_VALUE = Map.ofEntries(
            Map.entry("fighter", 1L),
            Map.entry("rogue", 2L),
            Map.entry("wizard", 3L),
            Map.entry("monk", 4L),
            Map.entry("sorcerer", 5L),
            Map.entry("cleric", 6L),
            Map.entry("barbarian", 7L),
            Map.entry("paladin", 8L),
            Map.entry("druid", 9L),
            Map.entry("ranger", 10L)
    );

    private final HeroImportSourceValueNormalizer normalizer;

    public Long resolveId(String sourceValue) {
        String normalizedValue = normalizer.normalize(sourceValue);
        return normalizedValue == null ? null : HERO_CLASS_ID_BY_SOURCE_VALUE.get(normalizedValue);
    }
}
