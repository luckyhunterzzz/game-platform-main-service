package com.gameplatform.mainservice.hero.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record HeroBatchLookupRequest(
        @NotEmpty List<Long> heroIds
) {
}
