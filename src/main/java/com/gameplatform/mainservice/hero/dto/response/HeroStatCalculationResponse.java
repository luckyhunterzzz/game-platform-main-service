package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import com.gameplatform.mainservice.hero.domain.enums.EvolutionStageCode;

public record HeroStatCalculationResponse(
        EvolutionStageCode stageCode,
        Long costumeHeroId,
        Integer costumeIndex,
        EmblemPathType emblemPathType,
        Boolean includeMasterEmblems,
        HeroStatBlockResponse minStats,
        HeroStatBlockResponse baseStats,
        HeroStatBlockResponse stageStats,
        HeroStatBlockResponse costumeBonus,
        HeroStatBlockResponse emblemBonus,
        HeroStatBlockResponse masterEmblemBonus,
        HeroStatBlockResponse finalStats,
        Integer finalPower
) {
}
