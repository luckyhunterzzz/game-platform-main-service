package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.util.UUID;

public record SummonProbabilityDefinitionResponse(
        UUID id,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson
) {
}
