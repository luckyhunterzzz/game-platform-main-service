package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.domain.enums.HeroPublicVisibilityMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HeroPublicVisibilityUpdateRequest(
        @NotNull(message = "mode is required")
        HeroPublicVisibilityMode mode,
        @NotBlank(message = "updatedBy is required")
        String updatedBy,
        String updatedByEmail
) {
}
