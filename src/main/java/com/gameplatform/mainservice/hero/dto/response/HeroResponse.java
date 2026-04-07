package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record HeroResponse(
        Long id,
        String slug,
        LocalizedTextJson nameJson,
        LocalizedTextJson specialSkillNameJson,
        LocalizedTextJson specialSkillDescriptionJson,
        Integer baseAttack,
        Integer baseArmor,
        Integer baseHp,
        Long elementId,
        Long rarityId,
        Long heroClassId,
        Long familyId,
        Long manaSpeedId,
        Long alphaTalentId,
        LocalizedTextJson imageBucketJson,
        LocalizedTextJson imageObjectKeyJson,
        LocalizedTextJson imageUrlJson,
        Boolean isCostume,
        Long baseHeroId,
        Integer costumeIndex,
        CostumeBonusJson costumeBonusJson,
        LocalDate releaseDate,
        HeroStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        String updatedBy,
        String updatedByEmail,
        List<Long> passiveSkillIds
) {
}
