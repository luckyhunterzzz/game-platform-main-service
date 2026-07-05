package com.gameplatform.mainservice.event.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record EventBlockUpsertRequest(
        @Valid
        @NotNull
        LocalizedTextJson nameJson,
        @Valid
        LocalizedTextJson descriptionJson,
        @Valid
        LocalizedTextJson imageBucketJson,
        @Valid
        LocalizedTextJson imageObjectKeyJson,
        boolean visible
) {
}
