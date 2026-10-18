package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SummonCreateRequest(
        @NotBlank
        @Size(max = 150)
        String slug,

        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @Valid
        LocalizedTextJson descriptionJson,

        @NotNull
        ContentStatus status,

        @Size(max = 255)
        String imageBucket,

        @Size(max = 1024)
        String imageObjectKey,

        @Size(max = 255)
        String backgroundImageBucket,

        @Size(max = 1024)
        String backgroundImageObjectKey,

        List<@NotNull @Valid SummonProbabilityCreateRequest> probabilities,

        List<@NotNull @Valid SummonBonusRuleCreateRequest> bonusRules
) {
}
