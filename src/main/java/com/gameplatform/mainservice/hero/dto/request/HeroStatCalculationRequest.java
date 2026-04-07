package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;
import jakarta.validation.constraints.NotNull;

public record HeroStatCalculationRequest(
        @NotNull
        EvolutionStageCode stageCode,

        Long costumeHeroId,

        EmblemPathType emblemPathType,

        Boolean includeMasterEmblems
) {
}
