package com.gameplatform.mainservice.hero.service.importer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class HeroImportAlphaTalentIdResolver {

    private static final Map<String, Long> ALPHA_TALENT_ID_BY_SOURCE_VALUE = Map.ofEntries(
            Map.entry("attack_up", 1L),
            Map.entry("ailment_immunity", 2L),
            Map.entry("rage", 3L),
            Map.entry("dodge", 4L),
            Map.entry("fiend_resist", 5L),
            Map.entry("regen", 6L),
            Map.entry("defense_up", 7L),
            Map.entry("damage_reduction", 8L),
            Map.entry("special_boost", 9L),
            Map.entry("bulwark", 10L),
            Map.entry("heal_increase", 11L),
            Map.entry("counterattack", 12L),
            Map.entry("boosted_regen", 13L),
            Map.entry("special_armor", 14L),
            Map.entry("mana_boost", 15L),
            Map.entry("gamble", 16L),
            Map.entry("vampire", 17L),
            Map.entry("taunt", 18L),
            Map.entry("ailment_reflect", 19L),
            Map.entry("revive", 20L),
            Map.entry("knights_endurance", 21L)
    );

    private final HeroImportSourceValueNormalizer normalizer;

    public Long resolveId(String sourceValue) {
        String normalizedValue = normalizer.normalize(sourceValue);
        return normalizedValue == null ? null : ALPHA_TALENT_ID_BY_SOURCE_VALUE.get(normalizedValue);
    }
}
