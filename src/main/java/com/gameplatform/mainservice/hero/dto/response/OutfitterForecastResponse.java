package com.gameplatform.mainservice.hero.dto.response;

import java.time.LocalDate;
import java.util.List;

public record OutfitterForecastResponse(
        LocalDate suggestedPreviousEventDate,
        LocalDate effectivePreviousEventDate,
        LocalDate targetDate,
        List<OutfitterHeroResponse> newlyAvailableHeroes
) {
}
