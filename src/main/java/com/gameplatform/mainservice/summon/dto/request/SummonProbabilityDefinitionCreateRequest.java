package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SummonProbabilityDefinitionCreateRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @Valid
        LocalizedTextJson descriptionJson
) {
}
