package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.EmblemPathType;
import java.math.BigDecimal;

public record HeroClassEmblemBonusProfileResponse(
        Long id,
        Long heroClassId,
        EmblemPathType pathType,
        Integer attackFlatBonus,
        Integer armorFlatBonus,
        Integer hpFlatBonus,
        BigDecimal attackPercentBonus,
        BigDecimal armorPercentBonus,
        BigDecimal hpPercentBonus,
        Integer masterAttackBonus,
        Integer masterArmorBonus,
        Integer masterHpBonus
) {}
