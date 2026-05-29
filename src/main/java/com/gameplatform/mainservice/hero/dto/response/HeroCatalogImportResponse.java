package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroCatalogImportResponse(
        boolean dryRun,
        int totalSourceHeroes,
        int matchedHeroes,
        int createdHeroes,
        int skippedExistingHeroes,
        int skippedUnresolvedHeroes,
        List<String> createdSlugs,
        List<HeroCatalogImportPlannedHeroResponse> plannedHeroes,
        List<HeroCatalogImportSkippedItemResponse> skippedHeroes
) {
}
