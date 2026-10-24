package com.gameplatform.mainservice.summon.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record SummonBonusStepProbabilityResponse(
        UUID probabilityId,
        BigDecimal valuePercent,
        Integer position
) {
}
