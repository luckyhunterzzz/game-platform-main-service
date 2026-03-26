package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroClassCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroClassUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroClassResponse;
import com.gameplatform.mainservice.hero.mapper.HeroClassResponseConverter;
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

    public HeroClassResponse create(HeroClassCreateRequest request) {
        return converter.toResponse(service.create(request));
    }

    public HeroClassResponse update(Long id, HeroClassUpdateRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}