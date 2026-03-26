package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;

public record HeroClassEmblemBonusProfileResponse(
        Long id,
        Long heroClassId,
        EmblemPathType pathType,
        Integer attackBonus,
        Integer armorBonus,
        Integer hpBonus
) {}