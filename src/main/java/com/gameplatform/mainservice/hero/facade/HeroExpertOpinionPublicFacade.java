package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionPublicResponse;
import com.gameplatform.mainservice.hero.service.HeroExpertOpinionPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroExpertOpinionPublicFacade {

    private final HeroExpertOpinionPublicService service;

    public List<HeroExpertOpinionPublicResponse> getAllByHeroSlug(String slug, HeroLanguage language) {
        return service.getAllByHeroSlug(slug, language);
    }
}
