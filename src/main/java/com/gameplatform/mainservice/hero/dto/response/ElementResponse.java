package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record ElementResponse(
        Long id,
        LocalizedTextJson nameJson,
        String imageBucket,
        String imageObjectKey,
        String imageUrl
) {
}
