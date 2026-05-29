package com.gameplatform.mainservice.hero.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalHeroCatalogPayload(
        List<ExternalHeroRecord> allHeroes
) {
}
