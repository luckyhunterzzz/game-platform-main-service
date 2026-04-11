package com.gameplatform.mainservice.publication.dto.response;

import com.gameplatform.mainservice.publication.domain.enums.PublicationType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PublicationResponse(
   UUID id,
   PublicationType type,
   String title,
   String content,
   String imageUrl,
   boolean pinned,
   boolean showInNewsFeed,
   OffsetDateTime publishedAt
) {}
