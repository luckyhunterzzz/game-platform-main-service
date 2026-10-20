package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.CacheNames;
import com.gameplatform.mainservice.hero.converter.HeroExpertOpinionResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionPublicResponse;
import com.gameplatform.mainservice.hero.repository.HeroExpertOpinionRepository;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.settings.service.HeroPublicVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroExpertOpinionPublicService {

    private final HeroRepository heroRepository;
    private final HeroExpertOpinionRepository heroExpertOpinionRepository;
    private final HeroExpertOpinionResponseConverter converter;
    private final HeroPublicVisibilityService heroPublicVisibilityService;

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_EXPERT_OPINIONS)
    public List<HeroExpertOpinionPublicResponse> getAllByHeroSlug(String slug, HeroLanguage language) {
        Hero hero = heroPublicVisibilityService.isDraftVisibleInPublicCatalog()
                ? heroRepository.findBySlugAndStatusIn(slug, List.of(ContentStatus.READY, ContentStatus.DRAFT))
                .orElseThrow(() -> new NotFoundException("Hero not found with slug: " + slug))
                : heroRepository.findBySlugAndStatus(slug, ContentStatus.READY)
                .orElseThrow(() -> new NotFoundException("Hero not found with slug: " + slug));

        return heroExpertOpinionRepository.findPublishedByHeroIdOrdered(hero.getId()).stream()
                .map(item -> converter.toPublicResponse(item, language.getJsonKey()))
                .toList();
    }
}

