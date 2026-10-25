package com.gameplatform.mainservice.summon.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record SummonProbabilityHeroSelectionRequest(
        @NotNull UUID probabilityId,
        @NotEmpty @Size(max = 10) List<@NotNull Long> heroIds
) {
}
