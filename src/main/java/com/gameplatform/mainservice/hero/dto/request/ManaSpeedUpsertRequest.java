package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ManaSpeedUpsertRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        LocalizedTextJson descriptionJson
) {
}
