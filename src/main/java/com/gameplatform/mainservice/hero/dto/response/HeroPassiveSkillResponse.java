package com.gameplatform.mainservice.hero.dto.response;

public record HeroPassiveSkillResponse(
        Long id,
        String name,
        String description,
        String imageUrl
) {
}
