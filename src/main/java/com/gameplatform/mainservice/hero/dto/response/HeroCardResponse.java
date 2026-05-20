package com.gameplatform.mainservice.hero.dto.response;

import java.time.LocalDate;

public record HeroCardResponse(
        Long id,
        String slug,
        String name,
        Long baseHeroId,
        Boolean isCostume,
        Integer costumeIndex,
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
        Integer baseHp,
        LocalDate releaseDate
) {
}
