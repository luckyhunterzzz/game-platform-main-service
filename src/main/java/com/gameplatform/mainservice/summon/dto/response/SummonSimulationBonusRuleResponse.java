package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;

import java.util.List;
import java.util.UUID;

public record SummonSimulationBonusRuleResponse(
        UUID id,
        SummonProbabilityDefinitionResponse definition,
        SummonBonusKind kind,
        boolean userSelectable,
        List<SummonSimulationBonusStepResponse> steps
) {
}
