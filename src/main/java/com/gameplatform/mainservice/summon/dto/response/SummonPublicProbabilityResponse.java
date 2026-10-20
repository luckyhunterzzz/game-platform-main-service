package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SummonPublicProbabilityResponse(
        UUID id,
        SummonProbabilityDefinitionResponse definition,
        SummonProbabilityKind kind,
        BigDecimal valuePercent,
        Integer position,
        boolean userSelectionEnabled,
        Integer defaultSelectedHeroCount,
        List<SummonHeroPreviewResponse> heroes
) {
}
