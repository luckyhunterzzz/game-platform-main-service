package com.gameplatform.mainservice.hero.dto.response;

public record HeroVariantSummaryResponse(
        Long id,
        String slug,
        String name,
        String imageUrl,
        String elementName,
        String rarityName,
        Integer rarityStars
) {
}
