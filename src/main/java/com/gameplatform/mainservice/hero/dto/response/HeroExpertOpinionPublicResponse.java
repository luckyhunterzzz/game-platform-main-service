package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.domain.enums.HeroExpertOpinionSourceType;

import java.time.LocalDate;

public record HeroExpertOpinionPublicResponse(
        Long id,
        String authorName,
        String sourceUrl,
        String sourceTitle,
        HeroExpertOpinionSourceType sourceType,
        String content,
        LocalDate publishedAt
) {
}
