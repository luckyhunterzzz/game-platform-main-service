package com.gameplatform.mainservice.summon.dto.response;

import java.util.List;
import java.util.UUID;

public record SummonSimulationResponse(
        UUID summonId,
        UUID variantId,
        Integer variantVersion,
        int requestedPullCount,
        int totalResultCount,
        List<SummonSimulationResultItemResponse> results
) {
}
