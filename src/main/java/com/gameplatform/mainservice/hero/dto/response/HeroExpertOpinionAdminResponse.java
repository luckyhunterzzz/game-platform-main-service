package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.HeroExpertOpinionSourceType;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record HeroExpertOpinionAdminResponse(
        Long id,
        Long heroId,
        String authorName,
        String sourceUrl,
        String sourceTitle,
        HeroExpertOpinionSourceType sourceType,
        LocalizedTextJson contentJson,
        Boolean isPublished,
        LocalDate publishedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
