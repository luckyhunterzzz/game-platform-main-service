package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record SummonHeroPreviewResponse(
        Long id,
        String slug,
        LocalizedTextJson nameJson,
        String previewUrl,
        Integer rarityStars
) {
}
