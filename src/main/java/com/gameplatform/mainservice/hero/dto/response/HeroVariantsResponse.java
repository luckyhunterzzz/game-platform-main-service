package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroVariantsResponse(
        HeroDetailsResponse currentHero,
        HeroVariantSummaryResponse baseHero,
        List<HeroVariantSummaryResponse> costumes
) {
}
