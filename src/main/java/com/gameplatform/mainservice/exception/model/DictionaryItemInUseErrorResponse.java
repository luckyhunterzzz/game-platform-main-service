package com.gameplatform.mainservice.exception.model;

import com.gameplatform.mainservice.hero.dto.response.HeroUsageReferenceResponse;

import java.time.OffsetDateTime;
import java.util.List;

public record DictionaryItemInUseErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        List<HeroUsageReferenceResponse> heroes
) {
}
