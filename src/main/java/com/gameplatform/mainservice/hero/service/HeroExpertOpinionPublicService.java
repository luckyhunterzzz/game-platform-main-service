package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroExpertOpinionResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.response.HeroExpertOpinionPublicResponse;
import com.gameplatform.mainservice.hero.repository.HeroExpertOpinionRepository;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroExpertOpinionPublicService {

    private final HeroRepository heroRepository;
    private final HeroExpertOpinionRepository heroExpertOpinionRepository;
    private final HeroExpertOpinionResponseConverter converter;

    public List<HeroExpertOpinionPublicResponse> getAllByHeroSlug(String slug, HeroLanguage language) {
        Hero hero = heroRepository.findBySlugAndStatus(slug, HeroStatus.READY)
                .orElseThrow(() -> new EntityNotFoundException("Hero not found with slug: " + slug));

        return heroExpertOpinionRepository.findPublishedByHeroIdOrdered(hero.getId()).stream()
                .map(item -> converter.toPublicResponse(item, language.getJsonKey()))
                .toList();
    }
}
