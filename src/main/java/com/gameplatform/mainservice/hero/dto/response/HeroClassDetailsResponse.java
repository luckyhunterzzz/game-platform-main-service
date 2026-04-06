package com.gameplatform.mainservice.hero.dto.response;

public record HeroClassDetailsResponse(
        Long id,
        String name,
        String baseName,
        String baseDescription,
        String masterName,
        String masterDescription
) {
}
