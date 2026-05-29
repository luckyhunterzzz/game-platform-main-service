package com.gameplatform.mainservice.hero.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record HeroCatalogImportRequest(
        @NotBlank
        @Size(max = 2048)
        String sourceUrl,

        @Size(max = 2048)
        String localizedSourceUrl,

        @NotNull
        @Min(1)
        @Max(5)
        Integer star,

        @NotNull
        HeroImportParentMode parentMode,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate releaseDateFrom,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate releaseDateTo,

        Boolean dryRun,

        @NotBlank
        @Size(max = 100)
        String updatedBy,

        @Size(max = 255)
        String updatedByEmail
) {
}
