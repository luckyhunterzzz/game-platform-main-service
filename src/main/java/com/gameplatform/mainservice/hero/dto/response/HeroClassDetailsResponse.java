package com.gameplatform.mainservice.hero.dto.response;

public record HeroClassDetailsResponse(
        Long id,
        String name,
        String imageUrl,
        String baseName,
        String baseDescription,
        String masterName,
        String masterDescription
) {
}
