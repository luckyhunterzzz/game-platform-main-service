package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record HeroTagReferenceResponse(
        Long id,
        LocalizedTextJson nameJson
) {
}
