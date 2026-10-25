package com.gameplatform.mainservice.summon.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SummonSimulationRequest(
        @NotNull Integer variantVersion,
        @NotNull Integer pullCount,
        @NotNull List<@NotNull @Valid SummonProbabilityHeroSelectionRequest> probabilityHeroSelections,
        @NotNull List<@NotNull @Valid SummonBonusStepHeroSelectionRequest> bonusStepHeroSelections,
        @NotNull List<@NotNull UUID> enabledBonusRuleIds
) {
}
