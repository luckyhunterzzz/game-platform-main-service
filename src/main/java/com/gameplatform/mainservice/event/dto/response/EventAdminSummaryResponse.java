package com.gameplatform.mainservice.event.dto.response;

import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.OffsetDateTime;

public record EventAdminSummaryResponse(
        Long id,
        String slug,
        EventStatus status,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        String imageUrl,
        int blockCount,
        OffsetDateTime updatedAt
) {
}
