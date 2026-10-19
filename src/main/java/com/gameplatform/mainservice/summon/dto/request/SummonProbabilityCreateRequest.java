package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SummonProbabilityCreateRequest(
        @NotNull
        UUID definitionId,

        @NotNull
        SummonProbabilityKind kind,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal valuePercent,

        @NotNull
        @Positive
        Integer position,

        List<@NotNull @Positive Long> heroIds
) {
}
