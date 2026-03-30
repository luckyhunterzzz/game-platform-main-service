package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroClassEmblemBonusProfileUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroClassEmblemBonusProfileResponse;
import com.gameplatform.mainservice.hero.converter.HeroClassEmblemBonusProfileResponseConverter;
import com.gameplatform.mainservice.hero.service.HeroClassEmblemBonusProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroClassEmblemBonusProfileFacade {

    private final HeroClassEmblemBonusProfileService service;
    private final HeroClassEmblemBonusProfileResponseConverter converter;

    public List<HeroClassEmblemBonusProfileResponse> getAll() {
        return converter.toResponseList(service.getAll());
    }

    public HeroClassEmblemBonusProfileResponse getById(Long id) {
        return converter.toResponse(service.getById(id));
    }

    public HeroClassEmblemBonusProfileResponse create(HeroClassEmblemBonusProfileCreateRequest request) {
        return converter.toResponse(service.create(request));
    }

    public HeroClassEmblemBonusProfileResponse update(Long id, HeroClassEmblemBonusProfileUpdateRequest request) {
        return converter.toResponse(service.update(id, request));
    }

    public void delete(Long id) {
        service.delete(id);
    }
}