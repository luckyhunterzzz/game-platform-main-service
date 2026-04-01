package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroClassUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroClassResponse;
import com.gameplatform.mainservice.hero.converter.HeroClassResponseConverter;
import com.gameplatform.mainservice.hero.service.HeroClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroClassFacade {

    private final HeroClassService service;
    private final HeroClassResponseConverter converter;

    public List<HeroClassResponse> getAll() {
        return converter.toResponseList(service.getAll());
    }

    public HeroClassResponse getById(Long id) {
        return converter.toResponse(service.getById(id));
    }

    public HeroClassResponse create(HeroClassUpsertRequest request) {
        return converter.toResponse(service.create(request));
    }

    public HeroClassResponse update(Long id, HeroClassUpsertRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}
