package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.request.HeroBatchLookupRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.service.HeroPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroPublicFacade {

    private final HeroPublicService heroPublicService;

    public List<HeroLookupResponse> getNames(HeroLanguage language) {
        return heroPublicService.getNames(language);
    }

    public List<HeroCardResponse> getHeroesBatch(HeroLanguage language, boolean includeDrafts, HeroBatchLookupRequest request) {
        return heroPublicService.getHeroesBatch(language, includeDrafts, request);
    }

    public List<HeroLookupResponse> search(String query, int limit, HeroLanguage language) {
        return heroPublicService.search(query, limit, language);
    }

    public HeroCatalogFiltersResponse getFilters(HeroLanguage language) {
        return heroPublicService.getFilters(language);
    }

    public HeroDetailsResponse getDetails(String slug, HeroLanguage language, boolean includeDrafts) {
        return heroPublicService.getDetails(slug, language, includeDrafts);
    }

    public HeroStatCalculationResponse calculateStats(
            String slug,
            HeroLanguage language,
            boolean includeDrafts,
            HeroStatCalculationRequest request
    ) {
        return heroPublicService.calculateStats(slug, language, includeDrafts, request);
    }

    public HeroPageResponse getHeroes(
            int page,
            int size,
            HeroLanguage language,
            String search,
            List<Long> elementIds,
            List<Long> rarityIds,
            List<Long> heroClassIds,
            List<Long> familyIds,
            List<Long> manaSpeedIds,
            List<Long> alphaTalentIds,
            boolean includeDrafts
    ) {
        return heroPublicService.getHeroes(
                page,
                size,
                language,
                search,
                elementIds,
                rarityIds,
                heroClassIds,
                familyIds,
                manaSpeedIds,
                alphaTalentIds,
                includeDrafts
        );
    }

    public HeroVariantsResponse getVariants(String slug, HeroLanguage language, boolean includeDrafts) {
        return heroPublicService.getVariants(slug, language, includeDrafts);
    }
}
