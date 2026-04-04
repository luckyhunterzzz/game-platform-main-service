package com.gameplatform.mainservice.publication.dto.response;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicationAdminDetailsResponse(
        UUID id,
        PublicationType type,
        PublicationStatus status,
        String title,
        String content,
        String imageBucket,
        String imageObjectKey,
        String imageUrl,
        boolean pinned,
        OffsetDateTime publishedAt
) {
}
