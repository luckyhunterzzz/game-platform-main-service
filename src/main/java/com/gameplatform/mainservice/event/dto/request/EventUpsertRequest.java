package com.gameplatform.mainservice.event.dto.request;

import com.gameplatform.mainservice.event.domain.enums.EventStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventUpsertRequest(
        @NotBlank(message = "must not be blank")
        String slug,
        @Valid
        @NotNull
        LocalizedTextJson nameJson,
        @Valid
        LocalizedTextJson descriptionJson,
        @NotNull
        EventStatus status,
        String imageBucket,
        String imageObjectKey
) {
}
