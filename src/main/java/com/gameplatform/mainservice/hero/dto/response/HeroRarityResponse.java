package com.gameplatform.mainservice.hero.dto.response;

public record HeroRarityResponse(
        Long id,
        int stars,
        String imageUrl
) {}
