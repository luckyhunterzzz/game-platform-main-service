package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.service.HeroAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroFacade {

    private final HeroAdminService heroService;

    public List<HeroResponse> getAll() {
        return heroService.getAll();
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
}
