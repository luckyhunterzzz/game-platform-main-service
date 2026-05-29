package com.gameplatform.mainservice.hero.dto.response;

public record HeroCatalogImportPlannedHeroResponse(
        String heroId,
        String name,
        String slug,
        boolean costume,
        String baseHeroSlug,
        String releaseDate,
        String status,
        String fullImageEnSourceUrl,
        String fullImageRuSourceUrl,
        String previewImageSourceUrl
) {
}
