package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record RarityResponse(
        Long id,
        LocalizedTextJson nameJson,
        Integer stars,
        String imageBucket,
        String imageObjectKey,
        String imageUrl
) {
}
