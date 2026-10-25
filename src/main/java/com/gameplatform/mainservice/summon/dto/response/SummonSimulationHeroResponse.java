package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record SummonSimulationHeroResponse(
        Long id,
        String slug,
        LocalizedTextJson nameJson,
        String previewUrl,
        LocalizedTextJson imageUrlJson,
        Integer rarityStars
) {
}
