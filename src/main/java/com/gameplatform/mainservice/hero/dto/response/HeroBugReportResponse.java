package com.gameplatform.mainservice.hero.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HeroBugReportResponse(
        UUID id,
        Long heroId,
        UUID authorId,
        String authorName,
        String description,
        boolean isOpen,
        OffsetDateTime createdAt,
        OffsetDateTime closedAt,
        String closedBy
) {
}
