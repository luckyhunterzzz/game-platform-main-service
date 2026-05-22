package com.gameplatform.mainservice.hero.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HeroCoachForecastResponse(
        LocalDate suggestedPreviousEventDate,
        LocalDate effectivePreviousEventDate,
        LocalDate targetDate,
        List<HeroCoachHeroResponse> newlyAvailableHeroes
) {
}
