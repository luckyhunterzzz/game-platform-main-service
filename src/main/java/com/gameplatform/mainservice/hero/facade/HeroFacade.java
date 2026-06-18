package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminVariantsResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroBugReportsAdminResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroNextCostumeIndexResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroStatCalculationResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSlugAvailabilityResponse;
import com.gameplatform.mainservice.hero.service.HeroAdminService;
import com.gameplatform.mainservice.hero.service.HeroStatCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroFacade {

    private final HeroAdminService heroService;
    private final HeroStatCalculationService heroStatCalculationService;

    public List<HeroResponse> getAll() {
        return heroService.getAll();
    }

    public HeroAdminPageResponse getCatalog(int page, int size, String search, List<Long> rarityIds, List<HeroStatus> statuses) {
        return heroService.getCatalog(page, size, search, rarityIds, statuses);
    }

    public HeroResponse getById(Long id) {
        return heroService.getResponseById(id);
    }

    public HeroAdminVariantsResponse getVariants(Long id, HeroLanguage language) {
        return heroService.getVariants(id, language);
    }

    public HeroBugReportsAdminResponse getBugReports(Long id) {
        return heroService.getBugReports(id);
    }

    public HeroSlugAvailabilityResponse getSlugAvailability(String slug, Long excludeId) {
        return heroService.getSlugAvailability(slug, excludeId);
    }

    public HeroNextCostumeIndexResponse getNextCostumeIndex(Long baseHeroId) {
        return heroService.getNextCostumeIndex(baseHeroId);
    }

    public HeroResponse create(HeroUpsertRequest request) {
        return heroService.buildResponse(heroService.create(request));
    }

    public HeroResponse update(Long id, HeroUpsertRequest request) {
        return heroService.buildResponse(heroService.update(id, request));
    }

    public void delete(Long id) {
        heroService.delete(id);
    }

    public HeroStatCalculationResponse calculateStats(Long heroId, HeroStatCalculationRequest request) {
        return heroStatCalculationService.calculate(heroId, request);
    }
}
