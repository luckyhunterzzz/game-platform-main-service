package com.gameplatform.mainservice.publication.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.publication.domain.enums.PublicationStatus;
import com.gameplatform.mainservice.publication.domain.enums.PublicationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record PublicationUpsertRequest(
        @Valid
        @NotNull
        LocalizedTextJson titleJson,
        @Valid
        LocalizedTextJson contentJson,
        @NotNull
        PublicationType type,
        @NotNull
        PublicationStatus status,
        String imageBucket,
        String imageObjectKey,
        boolean pinned,
        OffsetDateTime pinnedUntil,
        boolean showInNewsFeed,
        OffsetDateTime publishedAt
) {
}
