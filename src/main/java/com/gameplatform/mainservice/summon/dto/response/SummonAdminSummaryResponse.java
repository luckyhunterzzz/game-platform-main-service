package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SummonAdminSummaryResponse(
        UUID id,
        String slug,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        ContentStatus status,
        String imageUrl,
        String backgroundImageUrl,
        OffsetDateTime updatedAt
) {
}
