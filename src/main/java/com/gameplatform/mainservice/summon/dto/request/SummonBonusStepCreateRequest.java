package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record SummonBonusStepCreateRequest(
        @NotNull
        @Positive
        Integer position,

        @NotNull
        @DecimalMin("0.00")
        @DecimalMax("100.00")
        @Digits(integer = 3, fraction = 2)
        BigDecimal activationChancePercent,

        @NotNull
        BonusRewardMode rewardMode,

        @NotNull
        List<@NotNull @Valid SummonBonusStepProbabilityCreateRequest> rewardProbabilities
) {
}
