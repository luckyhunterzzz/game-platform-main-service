package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;

public record HeroCostumeBonusSummaryResponse(
        Integer costumeIndex,
        CostumeBonusJson bonus
) {
}
