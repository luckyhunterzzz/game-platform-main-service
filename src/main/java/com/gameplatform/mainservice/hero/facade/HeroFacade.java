package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroStatCalculationResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
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

    public HeroAdminPageResponse getCatalog(int page, int size, String search) {
        return heroService.getCatalog(page, size, search);
    }

    public HeroResponse getById(Long id) {
        return heroService.getResponseById(id);
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
