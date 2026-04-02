package com.gameplatform.mainservice.publication.dto.request;

import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record PublicationUpsertRequest(
        @NotBlank
        @Size(max = 500)
        String title,
        String content,
        @NotNull
        PublicationType type,
        @NotNull
        PublicationStatus status,
        String imageBucket,
        String imageObjectKey,
        boolean pinned,
        OffsetDateTime publishedAt
) {
}
