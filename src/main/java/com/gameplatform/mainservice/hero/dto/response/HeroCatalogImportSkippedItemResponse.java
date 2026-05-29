package com.gameplatform.mainservice.hero.dto.response;

public record HeroCatalogImportSkippedItemResponse(
        String heroId,
        String name,
        String slug,
        String reason
) {
}
