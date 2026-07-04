package com.gameplatform.mainservice.event.dto.response;

import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record EventAdminDetailsResponse(
        Long id,
        String slug,
        EventStatus status,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        String imageBucket,
        String imageObjectKey,
        String imageUrl,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<EventBlockAdminResponse> blocks
) {
}
