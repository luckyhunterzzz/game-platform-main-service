package com.gameplatform.mainservice.hero.dto.response;

public record HeroCatalogRarityFilterOptionResponse(
        Long id,
        String name,
        int stars,
        String imageUrl
) {
}
