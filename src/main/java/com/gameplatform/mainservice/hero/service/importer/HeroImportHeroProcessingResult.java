package com.gameplatform.mainservice.hero.service.importer;

import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportPlannedHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCatalogImportSkippedItemResponse;

public record HeroImportHeroProcessingResult(
        HeroImportHeroProcessingOutcome outcome,
        String slug,
        HeroCatalogImportPlannedHeroResponse plannedHero,
        HeroCatalogImportSkippedItemResponse skippedHero
) {
    public static HeroImportHeroProcessingResult created(String slug, HeroCatalogImportPlannedHeroResponse plannedHero) {
        return new HeroImportHeroProcessingResult(HeroImportHeroProcessingOutcome.CREATED, slug, plannedHero, null);
    }

    public static HeroImportHeroProcessingResult skippedExisting(HeroCatalogImportSkippedItemResponse skippedHero) {
        return new HeroImportHeroProcessingResult(HeroImportHeroProcessingOutcome.SKIPPED_EXISTING, skippedHero.slug(), null, skippedHero);
    }

    public static HeroImportHeroProcessingResult skippedUnresolved(HeroCatalogImportSkippedItemResponse skippedHero) {
        return new HeroImportHeroProcessingResult(HeroImportHeroProcessingOutcome.SKIPPED_UNRESOLVED, skippedHero.slug(), null, skippedHero);
    }

    public boolean isCreated() {
        return outcome == HeroImportHeroProcessingOutcome.CREATED;
    }

    public boolean isSkippedExisting() {
        return outcome == HeroImportHeroProcessingOutcome.SKIPPED_EXISTING;
    }

    public boolean isSkippedUnresolved() {
        return outcome == HeroImportHeroProcessingOutcome.SKIPPED_UNRESOLVED;
    }
}
