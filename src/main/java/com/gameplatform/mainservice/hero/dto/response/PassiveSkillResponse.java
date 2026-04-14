package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

public record PassiveSkillResponse(
        Long id,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        String imageBucket,
        String imageObjectKey,
        String imageUrl
) {}
