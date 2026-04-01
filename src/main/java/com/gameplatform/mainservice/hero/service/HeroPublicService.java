package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.hero.repository.projection.HeroCardProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroDetailsProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroVariantSummaryProjection;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroPublicService {

    private final HeroRepository heroRepository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;
    private final PassiveSkillRepository passiveSkillRepository;

    private final HeroPublicResponseConverter converter;

    public HeroPageResponse getHeroes(int page, int size, HeroLanguage language) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize);

        Page<HeroCardProjection> heroPage = heroRepository.findReadyBaseHeroCards(
                language.getJsonKey(),
                pageable
        );

        List<HeroCardResponse> items = heroPage.getContent().stream()
                .map(converter::toCardResponse)
                .toList();

        return new HeroPageResponse(
                items,
                heroPage.getNumber(),
                heroPage.getSize(),
                heroPage.getTotalElements(),
                heroPage.getTotalPages(),
                heroPage.hasNext()
        );
    }

    public List<HeroLookupResponse> getNames(HeroLanguage language) {
        String locale = language.getJsonKey();
        return converter.toLookupResponses(
                heroRepository.findAllReadyBaseHeroNames(locale)
        );
    }

    public List<HeroLookupResponse> search(String query, int limit, HeroLanguage language) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        String normalizedQuery = query.trim();
        if (normalizedQuery.length() < 3) {
            return List.of();
        }

        int normalizedLimit = Math.min(Math.max(limit, 1), 15);

        List<HeroSearchProjection> results = heroRepository.searchReadyBaseHeroesByName(
                normalizedQuery,
                language.getJsonKey(),
                normalizedLimit
        );

        return converter.toLookupResponses(results);
    }

    public HeroDetailsResponse getDetails(String slug, HeroLanguage language) {
        HeroDetailsProjection currentHero = findCurrentBaseHero(slug, language);
        return buildHeroDetails(currentHero, language.getJsonKey());
    }

    public HeroVariantsResponse getVariants(String slug, HeroLanguage language) {
        HeroDetailsProjection currentHero = findCurrentHero(slug, language);
        HeroVariantSummaryProjection baseHero = findBaseHero(currentHero, language);
        HeroDetailsResponse currentHeroDetails = buildHeroDetails(currentHero, language.getJsonKey());

        return new HeroVariantsResponse(
                currentHeroDetails,
                converter.toVariantSummary(baseHero),
                buildVariantCostumes(baseHero.getId(), language)
        );
    }

    private HeroDetailsResponse buildHeroDetails(HeroDetailsProjection hero, String locale) {
        List<PassiveSkill> passiveSkills = findPassiveSkills(hero.getId());
        List<Hero> costumes = findCostumes(hero);

        return converter.toDetailsResponse(
                hero,
                passiveSkills,
                costumes,
                locale
        );
    }

    private HeroDetailsProjection findCurrentBaseHero(String slug, HeroLanguage language) {
        return heroRepository.findReadyBaseHeroDetailsBySlug(slug, language.getJsonKey())
                .orElseThrow(() -> new EntityNotFoundException("Hero not found with slug: " + slug));
    }

    private HeroDetailsProjection findCurrentHero(String slug, HeroLanguage language) {
        return heroRepository.findReadyHeroDetailsBySlug(slug, language.getJsonKey())
                .orElseThrow(() -> new EntityNotFoundException("Hero not found with slug: " + slug));
    }

    private HeroVariantSummaryProjection findBaseHero(HeroDetailsProjection hero, HeroLanguage language) {
        if (!Boolean.TRUE.equals(hero.getIsCostume())) {
            return heroRepository.findReadyHeroVariantSummaryById(hero.getId(), language.getJsonKey())
                    .orElseThrow(() -> new EntityNotFoundException("Hero not found with id: " + hero.getId()));
        }

        Long baseHeroId = hero.getBaseHeroId();
        if (baseHeroId == null) {
            throw new EntityNotFoundException("Hero not found with baseHeroId: " + baseHeroId);
        }
        return heroRepository.findReadyHeroVariantSummaryById(baseHeroId, language.getJsonKey())
                .orElseThrow(() -> new EntityNotFoundException("Hero not found with baseHeroId: " + baseHeroId));
    }

    private List<PassiveSkill> findPassiveSkills(Long heroId) {
        List<Long> passiveIds = heroPassiveSkillRepository.findAllByIdHeroId(heroId).stream()
                .map(link -> link.getId().getPassiveSkillId())
                .toList();

        return passiveIds.isEmpty()
                ? List.of()
                : passiveSkillRepository.findAllByIdIn(passiveIds);
    }

    private List<Hero> findCostumes(HeroDetailsProjection hero) {
        Long variantsRootId = Boolean.TRUE.equals(hero.getIsCostume())
                ? hero.getBaseHeroId()
                : hero.getId();

        return variantsRootId == null
                ? List.of()
                : heroRepository.findAllByBaseHeroIdAndStatus(variantsRootId, HeroStatus.READY);
    }

    private List<HeroVariantSummaryResponse> buildVariantCostumes(Long baseHeroId, HeroLanguage language) {
        return heroRepository.findReadyHeroVariantSummariesByBaseHeroId(baseHeroId, language.getJsonKey())
                .stream()
                .map(converter::toVariantSummary)
                .toList();
    }
}
