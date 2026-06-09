package com.gameplatform.mainservice.config;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class PublicCacheEvictionService {

    @CacheEvict(
            cacheNames = {
                    CacheNames.PUBLIC_HEROES_PAGE,
                    CacheNames.PUBLIC_HERO_NAMES,
                    CacheNames.PUBLIC_HERO_BATCH,
                    CacheNames.PUBLIC_HERO_SEARCH,
                    CacheNames.PUBLIC_HERO_FILTERS,
                    CacheNames.PUBLIC_HERO_DETAILS,
                    CacheNames.PUBLIC_HERO_VARIANTS,
                    CacheNames.PUBLIC_HERO_EXPERT_OPINIONS,
                    CacheNames.PUBLIC_HERO_COACH_PAGE,
                    CacheNames.PUBLIC_HERO_COACH_FORECAST,
                    CacheNames.PUBLIC_OUTFITTER_PAGE,
                    CacheNames.PUBLIC_OUTFITTER_FORECAST
            },
            allEntries = true
    )
    public void evictHeroCaches() {
    }

    @CacheEvict(cacheNames = CacheNames.PUBLIC_PUBLICATIONS_FEED, allEntries = true)
    public void evictPublicationCaches() {
    }
}
