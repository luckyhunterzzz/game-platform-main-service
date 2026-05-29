package com.gameplatform.mainservice.hero.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalLocalizedHeroRecord(
        String id,
        String name,
        String description,
        List<String> imageUrls,
        String empuzzledName,
        String empuzzledHeroId
) {
}
