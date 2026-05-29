package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.HeroPublicVisibilityMode;

import java.time.OffsetDateTime;

public record HeroPublicVisibilityResponse(
        HeroPublicVisibilityMode mode,
        boolean includeDrafts,
        OffsetDateTime updatedAt,
        String updatedBy,
        String updatedByEmail
) {
}
