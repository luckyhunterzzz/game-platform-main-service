package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record FamilyUpdateRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @Valid
        LocalizedTextJson descriptionJson
) {
}