package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record ManaSpeedResponse(
        Long id,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson
) {}