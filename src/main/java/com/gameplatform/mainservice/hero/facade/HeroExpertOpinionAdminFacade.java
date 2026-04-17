package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.dto.request.HeroExpertOpinionUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionAdminResponse;
import com.gameplatform.mainservice.hero.service.HeroExpertOpinionAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroExpertOpinionAdminFacade {

    private final HeroExpertOpinionAdminService service;

    public List<HeroExpertOpinionAdminResponse> getAllByHeroId(Long heroId) {
        return service.getAllByHeroId(heroId);
    }

    public HeroExpertOpinionAdminResponse create(Long heroId, HeroExpertOpinionUpsertRequest request) {
        return service.create(heroId, request);
    }

    public HeroExpertOpinionAdminResponse update(Long heroId, Long opinionId, HeroExpertOpinionUpsertRequest request) {
        return service.update(heroId, opinionId, request);
    }

    public void delete(Long heroId, Long opinionId) {
        service.delete(heroId, opinionId);
    }
}
