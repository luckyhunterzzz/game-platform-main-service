package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record HeroUsageReferenceResponse(
        Long id,
        String slug,
        LocalizedTextJson nameJson,
        String status
) {
}
