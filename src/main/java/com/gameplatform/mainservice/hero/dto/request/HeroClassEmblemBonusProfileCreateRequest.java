package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import jakarta.validation.constraints.NotNull;

public record HeroClassEmblemBonusProfileCreateRequest(

        @NotNull
        Long heroClassId,

        @NotNull
        EmblemPathType pathType,

        @NotNull
        Integer attackBonus,

        @NotNull
        Integer armorBonus,

        @NotNull
        Integer hpBonus
) {}