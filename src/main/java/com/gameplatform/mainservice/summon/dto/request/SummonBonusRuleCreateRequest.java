package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record SummonBonusRuleCreateRequest(
        @NotNull
        UUID definitionId,

        @NotNull
        SummonBonusKind kind,

        @NotNull
        Boolean userSelectable,

        @Valid
        SummonBonusConditionRequest triggerCondition,

        @Valid
        SummonBonusConditionRequest continuationCondition,

        @NotEmpty
        List<@NotNull @Valid SummonBonusStepCreateRequest> steps
) {
}
