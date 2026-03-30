package com.gameplatform.mainservice.hero.dto.response;

public record HeroSimpleNameResponse(
        Long id,
        String slug,
        String name
) {
}