package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record HeroClassResponse(
        Long id,
        LocalizedTextJson nameJson,
        LocalizedTextJson baseNameJson,
        LocalizedTextJson baseDescriptionJson,
        LocalizedTextJson masterNameJson,
        LocalizedTextJson masterDescriptionJson
) {}