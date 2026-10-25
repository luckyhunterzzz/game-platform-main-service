package com.gameplatform.mainservice.summon.dto.response;

import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SummonSimulationConfigResponse(
        UUID summonId,
        String slug,
        LocalizedTextJson nameJson,
        LocalizedTextJson descriptionJson,
        String imageUrl,
        String backgroundImageUrl,
        UUID variantId,
        Integer variantVersion,
        SummonVariantType variantType,
        OffsetDateTime startsAt,
        OffsetDateTime endsAt,
        List<SummonSimulationProbabilityResponse> probabilities,
        List<SummonSimulationBonusRuleResponse> bonusRules,
        List<SummonSimulationHeroResponse> selectableFiveStarHeroes
) {
}
