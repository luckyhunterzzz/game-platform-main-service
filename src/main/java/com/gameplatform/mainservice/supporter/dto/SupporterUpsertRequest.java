package com.gameplatform.mainservice.supporter.dto;

import com.gameplatform.mainservice.supporter.domain.SupporterFontStyle;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SupporterUpsertRequest(
        @NotBlank @Size(max = 100) String nickname,
        @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
        @NotNull SupporterFontStyle fontStyle,
        boolean active,
        @Min(0) @Max(9999) int sortOrder
) {
}
