package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;

import java.math.BigDecimal;
import java.util.UUID;

public record SummonSimulationBonusStepResponse(
        UUID id,
        Integer position,
        BigDecimal activationChancePercent,
        BonusRewardMode rewardMode,
        boolean userHeroSelectionEnabled,
        SummonSimulationHeroResponse fixedHero
) {
}
