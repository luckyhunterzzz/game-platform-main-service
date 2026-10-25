package com.gameplatform.mainservice.summon.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SummonBonusStepProbabilityCreateRequest(
        @NotNull UUID probabilityDefinitionId,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") @Digits(integer = 3, fraction = 2)
        BigDecimal valuePercent,
        @NotNull @Positive Integer position
) {
}
