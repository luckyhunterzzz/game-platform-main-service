package com.gameplatform.mainservice.hero.service.importer;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class HeroImportRarityIdResolver {

    private static final Map<Integer, Long> RARITY_ID_BY_STAR = Map.of(
            3, 3L,
            4, 2L,
            5, 1L
    );

    public Long resolveId(Integer star) {
        return star == null ? null : RARITY_ID_BY_STAR.get(star);
    }
}
