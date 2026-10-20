package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record SummonVariantUpdateRequest(
        @NotNull
        OffsetDateTime startsAt,

        @NotNull
        OffsetDateTime endsAt,

        @NotNull
        ContentStatus status,

        List<@NotNull @Valid SummonProbabilityCreateRequest> probabilities,

        List<@NotNull @Valid SummonBonusRuleCreateRequest> bonusRules
) {
}
