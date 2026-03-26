package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RarityEvolutionMultiplierCreateRequest(

        @NotNull
        Long rarityId,

        @NotNull
        EvolutionStageCode stageCode,

        @NotNull
        BigDecimal attackMultiplier,

        @NotNull
        BigDecimal armorMultiplier,

        @NotNull
        BigDecimal hpMultiplier
) {}