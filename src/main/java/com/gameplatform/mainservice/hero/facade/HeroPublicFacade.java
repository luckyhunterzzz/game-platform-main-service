package com.gameplatform.mainservice.hero.facade;

import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroDetailsResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSearchResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSimpleNameResponse;
import com.gameplatform.mainservice.hero.service.HeroPublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HeroPublicFacade {

    private final HeroPublicService heroPublicService;

    public List<HeroSimpleNameResponse> getNames(HeroLanguage language) {
        return heroPublicService.getNames(language);
    }

    public List<HeroSearchResponse> search(String query, int limit, HeroLanguage language) {
        return heroPublicService.search(query, limit, language);
    }

    public HeroDetailsResponse getDetails(String slug, HeroLanguage language) {
        return heroPublicService.getDetails(slug, language);
    }

    public HeroPageResponse getHeroes(int page, int size, HeroLanguage language) {
        return heroPublicService.getHeroes(page, size, language);
    }
}