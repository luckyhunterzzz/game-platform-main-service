package com.gameplatform.mainservice.hero.dto.response;

public record HeroCardResponse(
        Long id,
        String slug,
        String name,
        String imageUrl,
        String previewUrl,
        String elementName,
        String rarityName,
        Integer rarityStars,
        String heroClassName,
        String manaSpeedName,
        String familyName,
        String alphaTalentName,
        Integer baseAttack,
        Integer baseArmor,
        Integer baseHp
) {
}
