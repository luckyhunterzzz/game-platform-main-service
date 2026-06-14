package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;

import java.time.LocalDate;
import java.util.List;

public record HeroDetailsResponse(
        Long id,
        String slug,
        String name,

        SimpleReferenceResponse element,
        HeroRarityResponse rarity,
        HeroClassDetailsResponse heroClass,
        DescribedReferenceResponse family,
        DescribedReferenceResponse manaSpeed,
        DescribedReferenceResponse alphaTalent,
        List<DescribedReferenceResponse> roleGroups,
        List<DescribedReferenceResponse> tags,

        SpecialSkillResponse specialSkill,
        List<HeroPassiveSkillResponse> passiveSkills,

        List<HeroCostumeResponse> costumes,
        Long baseHeroId,
        Integer baseAttack,
        Integer baseArmor,
        Integer baseHp,
        Integer basePower,
        CostumeBonusJson costumeBonusJson,

        String imageUrl,
        String previewUrl,
        LocalDate releaseDate,
        LocalDate heroCoachDate,
        LocalDate visitingOutfitterDate
) {}
