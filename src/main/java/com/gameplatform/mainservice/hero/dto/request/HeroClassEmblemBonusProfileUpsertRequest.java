package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record HeroClassEmblemBonusProfileUpsertRequest(

        @NotNull
        Long heroClassId,

        @NotNull
        EmblemPathType pathType,

        @NotNull
        Integer attackFlatBonus,

        @NotNull
        Integer armorFlatBonus,

        @NotNull
        Integer hpFlatBonus,

        @NotNull
        BigDecimal attackPercentBonus,

        @NotNull
        BigDecimal armorPercentBonus,

        @NotNull
        BigDecimal hpPercentBonus,

        @NotNull
        Integer masterAttackBonus,

        @NotNull
        Integer masterArmorBonus,

        @NotNull
        Integer masterHpBonus
) {
}
