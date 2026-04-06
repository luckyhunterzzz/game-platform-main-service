package com.gameplatform.mainservice.hero.dto.response;

import java.util.List;

public record HeroAdminPageResponse(
        List<HeroResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
}
