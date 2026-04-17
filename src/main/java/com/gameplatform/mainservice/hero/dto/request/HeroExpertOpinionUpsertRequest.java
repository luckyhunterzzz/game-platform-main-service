package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.HeroExpertOpinionSourceType;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HeroExpertOpinionUpsertRequest(
        @NotBlank
        @Size(max = 120)
        String authorName,

        @Size(max = 2048)
        String sourceUrl,

        @Size(max = 255)
        String sourceTitle,

        HeroExpertOpinionSourceType sourceType,

        @NotNull
        @Valid
        LocalizedTextJson contentJson,

        @NotNull
        Boolean isPublished,

        LocalDate publishedAt
) {
}
