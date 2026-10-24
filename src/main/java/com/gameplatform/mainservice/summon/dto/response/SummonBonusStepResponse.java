package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SummonBonusStepResponse(
        UUID id,
        Integer position,
        BigDecimal activationChancePercent,
        BonusRewardMode rewardMode,
        List<SummonBonusStepProbabilityResponse> rewardProbabilities,
        boolean userHeroSelectionEnabled,
        List<Long> rewardHeroIds
) {
}
