package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.util.List;

public record HeroTagGroupResponse(
        Long id,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        List<HeroTagReferenceResponse> tags
) {
}
