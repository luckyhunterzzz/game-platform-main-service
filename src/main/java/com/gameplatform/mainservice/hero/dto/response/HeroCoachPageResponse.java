package com.gameplatform.mainservice.hero.dto.response;

import java.time.LocalDate;
import java.util.List;

public record HeroCoachPageResponse(
        LocalDate suggestedPreviousEventDate,
        List<HeroCoachHeroResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
