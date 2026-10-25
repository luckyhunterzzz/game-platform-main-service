package com.gameplatform.mainservice.summon.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SummonSimulationProbabilityResponse(
        UUID id,
        SummonProbabilityDefinitionResponse definition,
        BigDecimal valuePercent,
        Integer position,
        boolean userSelectionEnabled,
        Integer defaultSelectedHeroCount,
        List<SummonSimulationHeroResponse> configuredHeroes
) {
}
