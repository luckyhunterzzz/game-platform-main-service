package com.gameplatform.mainservice.hero.service.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HeroImportManaSpeedIdResolver {

    private static final Map<String, Long> MANA_SPEED_ID_BY_SOURCE_VALUE = Map.ofEntries(
            Map.entry("very_fast", 1L),
            Map.entry("average", 2L),
            Map.entry("fast", 3L),
            Map.entry("slow", 4L),
            Map.entry("very_slow", 5L),
            Map.entry("changing_tides", 6L),
            Map.entry("charge_ninja", 7L),
            Map.entry("charge_magic", 8L),
            Map.entry("dancer", 9L),
            Map.entry("slayer", 10L),
            Map.entry("styx", 11L)
    );

    private final HeroImportSourceValueNormalizer normalizer;

    public Long resolveId(String sourceValue) {
        String normalizedValue = normalizer.normalize(sourceValue);
        return normalizedValue == null ? null : MANA_SPEED_ID_BY_SOURCE_VALUE.get(normalizedValue);
    }
}
