package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HeroTagUpsertRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @Valid
        LocalizedTextJson descriptionJson,

        @NotNull
        @Positive
        Long groupId
) {
}
