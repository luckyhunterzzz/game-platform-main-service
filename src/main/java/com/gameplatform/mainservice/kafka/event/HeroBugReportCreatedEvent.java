package com.gameplatform.mainservice.kafka.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HeroBugReportCreatedEvent(
        UUID eventId,
        UUID bugReportId,
        Long heroId,
        String heroSlug,
        String heroName,
        UUID authorId,
        String authorName,
        String description,
        OffsetDateTime createdAt
) {
}
