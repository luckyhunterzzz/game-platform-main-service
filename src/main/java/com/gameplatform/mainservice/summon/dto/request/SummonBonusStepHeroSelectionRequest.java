package com.gameplatform.mainservice.summon.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SummonBonusStepHeroSelectionRequest(
        @NotNull UUID bonusStepId,
        @NotNull Long heroId
) {
}
