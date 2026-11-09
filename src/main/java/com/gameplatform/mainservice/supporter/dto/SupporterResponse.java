package com.gameplatform.mainservice.supporter.dto;

import com.gameplatform.mainservice.supporter.domain.SupporterFontStyle;

import java.time.OffsetDateTime;

public record SupporterResponse(
        Long id,
        String nickname,
        String color,
        SupporterFontStyle fontStyle,
        boolean active,
        int sortOrder,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
