package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroPageResponse(
        List<HeroCardResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}