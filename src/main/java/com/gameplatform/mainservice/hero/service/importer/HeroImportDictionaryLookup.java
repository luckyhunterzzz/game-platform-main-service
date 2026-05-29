package com.gameplatform.mainservice.hero.service.importer;

import java.util.Map;

public record HeroImportDictionaryLookup(
        Map<String, Long> familyIdByAlias,
        Map<String, Long> alphaTalentIdByAlias
) {
}
