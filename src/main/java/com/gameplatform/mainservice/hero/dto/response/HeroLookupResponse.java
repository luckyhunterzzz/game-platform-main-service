package com.gameplatform.mainservice.hero.dto.response;

public record HeroLookupResponse(
        Long id,
        String slug,
        String name
) {
}
