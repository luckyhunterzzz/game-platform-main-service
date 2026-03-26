package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;

import java.math.BigDecimal;

public record RarityEvolutionMultiplierResponse(

        Long id,
        Long rarityId,
        EvolutionStageCode stageCode,
        BigDecimal attackMultiplier,
        BigDecimal armorMultiplier,
        BigDecimal hpMultiplier
) {}