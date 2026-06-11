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

public record HeroUpsertRequest(
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

        @PositiveOrZero
        Integer baseAttack,

        @PositiveOrZero
        Integer baseArmor,

        @PositiveOrZero
        Integer baseHp,

        @PositiveOrZero
        Integer basePower,

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

        @Valid
        LocalizedTextJson imageBucketJson,

        @Valid
        LocalizedTextJson imageObjectKeyJson,

        @Size(max = 255)
        String previewBucket,

        @Size(max = 1024)
        String previewObjectKey,

        @NotNull
        Boolean isCostume,

        Long baseHeroId,

        @Positive
        Integer costumeIndex,

        @Valid
        CostumeBonusJson costumeBonusJson,

        LocalDate releaseDate,

        @NotNull
        HeroStatus status,

        @NotBlank
        @Size(max = 100)
        String updatedBy,

        @Size(max = 255)
        String updatedByEmail,

        List<Long> passiveSkillIds,

        List<Long> tagIds
) {
}
