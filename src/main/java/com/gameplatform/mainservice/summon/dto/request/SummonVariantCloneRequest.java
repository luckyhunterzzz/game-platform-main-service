package com.gameplatform.mainservice.summon.dto.request;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SummonVariantCloneRequest(
        @NotNull
        UUID sourceVariantId,

        @NotNull
        OffsetDateTime startsAt,

        @NotNull
        OffsetDateTime endsAt,

        @NotNull
        ContentStatus status
) {
}
