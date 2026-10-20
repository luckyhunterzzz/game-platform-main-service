package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;

import java.util.UUID;

public record SummonBonusConditionResponse(
        BonusResultConditionType type,
        Long rarityId,
        UUID probabilityId
) {
}
