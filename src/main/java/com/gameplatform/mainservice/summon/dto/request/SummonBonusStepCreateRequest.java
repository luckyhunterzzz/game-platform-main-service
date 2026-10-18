package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record SummonBonusStepCreateRequest(
        @NotNull
        @Positive
        Integer position,

        @NotNull
        @DecimalMin("0.0000")
        @DecimalMax("100.0000")
        @Digits(integer = 3, fraction = 4)
        BigDecimal activationChancePercent,

        @NotNull
        BonusRewardMode rewardMode,

        UUID rewardProbabilityDefinitionId
) {
}
