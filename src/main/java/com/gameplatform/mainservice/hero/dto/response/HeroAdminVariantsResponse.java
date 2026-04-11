package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroAdminVariantsResponse(
        HeroVariantSummaryResponse currentHero,
        HeroVariantSummaryResponse baseHero,
        List<HeroVariantSummaryResponse> costumes
) {
}
