package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.util.UUID;

public record SummonPublicResponse(
        UUID id,
        String slug,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        String imageUrl,
        String backgroundImageUrl,
        SummonPublicVariantResponse variant
) {
}
