package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.service.HeroService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroFacade {

    private final HeroService heroService;

    public List<HeroResponse> getAll() {
        return heroService.getAll().stream()
                .map(heroService::buildResponse)
                .toList();
    }

    public HeroResponse getById(Long id) {
        return heroService.buildResponse(heroService.getById(id));
    }

    public HeroResponse create(HeroCreateRequest request) {
        return heroService.buildResponse(heroService.create(request));
    }

    public HeroResponse update(Long id, HeroUpdateRequest request) {
        return heroService.buildResponse(heroService.update(id, request));
    }

    public void delete(Long id) {
        heroService.delete(id);
    }
}