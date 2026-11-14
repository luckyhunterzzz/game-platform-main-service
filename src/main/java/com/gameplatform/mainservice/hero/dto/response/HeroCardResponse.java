package com.gameplatform.mainservice.hero.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HeroCardResponse(
        Long id,
        String slug,
        String name,
        Long baseHeroId,
        Boolean isCostume,
        Integer costumeIndex,
        Integer availableCostumeCount,
        String imageUrl,
        String previewUrl,
        String elementName,
        String elementImageUrl,
        String rarityName,
        Integer rarityStars,
        String heroClassName,
        String heroClassImageUrl,
        String manaSpeedName,
        String familyName,
        String alphaTalentName,
        Integer baseAttack,
        Integer baseArmor,
        Integer baseHp,
        LocalDate releaseDate,
        List<HeroCostumeBonusSummaryResponse> costumeBonuses
) {
}
