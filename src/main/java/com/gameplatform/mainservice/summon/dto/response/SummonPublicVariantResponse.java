package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SummonPublicVariantResponse(
        UUID id,
        SummonVariantType variantType,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        List<SummonPublicProbabilityResponse> probabilities
) {
}
