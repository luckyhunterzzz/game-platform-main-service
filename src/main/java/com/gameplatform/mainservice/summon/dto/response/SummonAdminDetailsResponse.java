package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SummonAdminDetailsResponse(
        UUID id,
        Integer version,
        String slug,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        ContentStatus status,
        String imageBucket,
        String imageObjectKey,
        String imageUrl,
        String backgroundImageBucket,
        String backgroundImageObjectKey,
        String backgroundImageUrl,
        UUID createdBy,
        UUID updatedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        SummonVariantResponse defaultVariant,
        List<SummonVariantResponse> scheduledVariants
) {
}
