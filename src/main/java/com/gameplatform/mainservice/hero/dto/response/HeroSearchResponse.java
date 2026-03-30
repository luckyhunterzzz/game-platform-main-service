package com.gameplatform.mainservice.hero.dto.response;

public record HeroSearchResponse(
        Long id,
        String slug,
        String name
) {
}