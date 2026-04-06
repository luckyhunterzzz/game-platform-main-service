package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroCatalogFiltersResponse(
        List<HeroCatalogFilterOptionResponse> elements,
        List<HeroCatalogRarityFilterOptionResponse> rarities,
        List<HeroCatalogFilterOptionResponse> heroClasses,
        List<HeroCatalogFilterOptionResponse> families,
        List<HeroCatalogFilterOptionResponse> manaSpeeds,
        List<HeroCatalogFilterOptionResponse> alphaTalents
) {
}
