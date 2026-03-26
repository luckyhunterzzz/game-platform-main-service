package com.gameplatform.mainservice.hero.dto.request;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PassiveSkillCreateRequest(
        @NotNull
        @Valid
        LocalizedTextJson nameJson,

        @NotNull
        @Valid
        LocalizedTextJson descriptionJson
) {}