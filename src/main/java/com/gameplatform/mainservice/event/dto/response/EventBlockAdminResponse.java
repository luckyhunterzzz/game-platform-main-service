package com.gameplatform.mainservice.event.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.OffsetDateTime;
import java.util.UUID;

public record EventBlockAdminResponse(
        Long id,
        int position,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        String imageBucket,
        String imageObjectKey,
        String imageUrl,
        boolean visible,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
