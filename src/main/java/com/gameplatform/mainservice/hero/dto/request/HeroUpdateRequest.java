package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record HeroUpdateRequest(
        @NotBlank
        @Size(max = 150)
        String slug,

        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @NotNull
        @Valid
        LocalizedTextJson specialSkillNameJson,

        @NotNull
        @Valid
        LocalizedTextJson specialSkillDescriptionJson,

        @NotNull
        @PositiveOrZero
        Integer baseAttack,

        @NotNull
        @PositiveOrZero
        Integer baseArmor,

        @NotNull
        @PositiveOrZero
        Integer baseHp,

        @NotNull
        @Positive
        Long elementId,

        @NotNull
        @Positive
        Long rarityId,

        @NotNull
        @Positive
        Long heroClassId,

        Long familyId,

        @NotNull
        @Positive
        Long manaSpeedId,

        Long alphaTalentId,

        @Size(max = 100)
        String imageBucket,

        @Size(max = 500)
        String imageObjectKey,

        @NotNull
        Boolean isCostume,

        Long baseHeroId,

        @Valid
        CostumeBonusJson costumeBonusJson,

        LocalDate releaseDate,

        @NotNull
        HeroStatus status,

        @NotBlank
        @Size(max = 100)
        String updatedBy,

        List<Long> passiveSkillIds
) {
}