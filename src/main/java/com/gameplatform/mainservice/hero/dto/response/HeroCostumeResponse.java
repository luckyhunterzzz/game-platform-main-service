package com.gameplatform.mainservice.hero.dto.response;

import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;

public record HeroCostumeResponse(
        Long id,
        String slug,
        String name,
        Integer costumeIndex,
        CostumeBonusJson bonus
) {}
