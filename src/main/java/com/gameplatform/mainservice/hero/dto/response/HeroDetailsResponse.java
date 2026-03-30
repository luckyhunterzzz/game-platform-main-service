package com.gameplatform.mainservice.hero.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HeroDetailsResponse(
        Long id,
        String slug,
        String name,

        SimpleReferenceResponse element,
        HeroRarityResponse rarity,
        SimpleReferenceResponse heroClass,
        SimpleReferenceResponse family,
        SimpleReferenceResponse manaSpeed,
        SimpleReferenceResponse alphaTalent,

        SpecialSkillResponse specialSkill,
        List<HeroPassiveSkillResponse> passiveSkills,

        List<HeroCostumeResponse> costumes,
        Long baseHeroId,

        String imageUrl,
        LocalDate releaseDate
) {}