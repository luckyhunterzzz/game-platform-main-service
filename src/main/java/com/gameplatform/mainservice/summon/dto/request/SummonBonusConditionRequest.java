package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record SummonBonusConditionRequest(
        BonusResultConditionType type,

        @Positive
        Long rarityId,

        UUID probabilityDefinitionId
) {
}
