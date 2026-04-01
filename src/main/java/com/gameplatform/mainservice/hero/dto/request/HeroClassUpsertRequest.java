package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record HeroClassUpsertRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @NotNull
        @Valid
        LocalizedTextJson baseNameJson,

        @NotNull
        @Valid
        LocalizedTextJson baseDescriptionJson,

        @NotNull
        @Valid
        LocalizedTextJson masterNameJson,

        @NotNull
        @Valid
        LocalizedTextJson masterDescriptionJson
) {
}
