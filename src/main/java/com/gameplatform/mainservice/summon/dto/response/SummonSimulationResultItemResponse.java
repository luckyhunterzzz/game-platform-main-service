package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.SummonSimulationResultSource;

import java.util.UUID;

public record SummonSimulationResultItemResponse(
        int sequence,
        int rootPullNumber,
        Integer parentResultSequence,
        SummonSimulationResultSource source,
        UUID probabilityId,
        UUID bonusRuleId,
        UUID bonusStepId,
        SummonSimulationHeroResponse hero
) {
}
