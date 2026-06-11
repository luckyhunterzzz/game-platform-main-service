package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HeroTagGroupUpsertRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @Valid
        LocalizedTextJson descriptionJson,

        List<Long> tagIds
) {
}
