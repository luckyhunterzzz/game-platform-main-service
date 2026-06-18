package com.gameplatform.mainservice.hero.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BugReportCreateRequest(
        @NotBlank
        @Size(max = 100)
        String authorName,

        @NotBlank
        @Size(max = 2000)
        String description
) {
}
