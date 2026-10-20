package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SummonVariantResponse(
        UUID id,
        Integer version,
        SummonVariantType variantType,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        ContentStatus status,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<SummonProbabilityResponse> probabilities,
        List<SummonBonusRuleResponse> bonusRules
) {
}
