package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroPublicService {

    private final HeroRepository heroRepository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;
    private final PassiveSkillRepository passiveSkillRepository;
    private final ElementRepository elementRepository;
    private final RarityRepository rarityRepository;
    private final HeroClassRepository heroClassRepository;
    private final FamilyRepository familyRepository;
    private final ManaSpeedRepository manaSpeedRepository;
    private final AlphaTalentRepository alphaTalentRepository;

    private final HeroPublicResponseConverter converter;

    public HeroPageResponse getHeroes(int page, int size, HeroLanguage language) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageable = PageRequest.of(
                normalizedPage,
                normalizedSize,
                Sort.by(
                        Sort.Order.asc("nameJson"),
                        Sort.Order.asc("id")
                )
        );

        Page<Hero> heroPage = heroRepository.findAllByStatusAndIsCostumeFalse(
                HeroStatus.READY,
                pageable
        );

        List<HeroCardResponse> items = heroPage.getContent().stream()
                .map(hero -> {
                    Element element = elementRepository.findById(hero.getElementId())
                            .orElseThrow(() -> new EntityNotFoundException("Element not found: " + hero.getElementId()));

                    Rarity rarity = rarityRepository.findById(hero.getRarityId())
                            .orElseThrow(() -> new EntityNotFoundException("Rarity not found: " + hero.getRarityId()));

                    HeroClass heroClass = heroClassRepository.findById(hero.getHeroClassId())
                            .orElseThrow(() -> new EntityNotFoundException("HeroClass not found: " + hero.getHeroClassId()));

                    ManaSpeed manaSpeed = manaSpeedRepository.findById(hero.getManaSpeedId())
                            .orElseThrow(() -> new EntityNotFoundException("ManaSpeed not found: " + hero.getManaSpeedId()));

                    Family family = hero.getFamilyId() != null
                            ? familyRepository.findById(hero.getFamilyId()).orElse(null)
                            : null;

                    AlphaTalent alphaTalent = hero.getAlphaTalentId() != null
                            ? alphaTalentRepository.findById(hero.getAlphaTalentId()).orElse(null)
                            : null;

                    return converter.toCardResponse(
                            hero,
                            element,
                            rarity,
                            heroClass,
                            family,
                            manaSpeed,
                            alphaTalent,
                            language
                    );
                })
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

    public List<HeroSimpleNameResponse> getNames(HeroLanguage language) {
        String locale = language.getJsonKey();
        return converter.toSimpleNameList(
                heroRepository.findAllReadyBaseHeroNames(locale)
        );
    }

    public List<HeroSearchResponse> search(String query, int limit, HeroLanguage language) {
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

        return converter.toSearchResponses(results);
    }

    public HeroDetailsResponse getDetails(String slug, HeroLanguage language) {
        Hero hero = heroRepository.findBySlugAndStatusAndIsCostumeFalse(slug, HeroStatus.READY)
                .orElseThrow(() -> new EntityNotFoundException("Hero not found with slug: " + slug));

        List<HeroPassiveSkill> passiveLinks = heroPassiveSkillRepository.findAllByIdHeroId(hero.getId());
        List<Long> passiveIds = passiveLinks.stream()
                .map(link -> link.getId().getPassiveSkillId())
                .toList();

        List<PassiveSkill> passiveSkills = passiveIds.isEmpty()
                ? List.of()
                : passiveSkillRepository.findAllByIdIn(passiveIds);

        List<Hero> costumes = heroRepository.findAllByBaseHeroIdAndStatus(hero.getId(), HeroStatus.READY);

        Element element = elementRepository.findById(hero.getElementId())
                .orElseThrow(() -> new EntityNotFoundException("Element not found"));

        Rarity rarity = rarityRepository.findById(hero.getRarityId())
                .orElseThrow(() -> new EntityNotFoundException("Rarity not found"));

        HeroClass heroClass = heroClassRepository.findById(hero.getHeroClassId())
                .orElseThrow(() -> new EntityNotFoundException("HeroClass not found"));

        ManaSpeed manaSpeed = manaSpeedRepository.findById(hero.getManaSpeedId())
                .orElseThrow(() -> new EntityNotFoundException("ManaSpeed not found"));

        Family family = hero.getFamilyId() != null
                ? familyRepository.findById(hero.getFamilyId()).orElse(null)
                : null;

        AlphaTalent alphaTalent = hero.getAlphaTalentId() != null
                ? alphaTalentRepository.findById(hero.getAlphaTalentId()).orElse(null)
                : null;

        return converter.toDetailsResponse(
                hero,
                element,
                rarity,
                heroClass,
                family,
                manaSpeed,
                alphaTalent,
                passiveSkills,
                costumes,
                language
        );
    }

    public HeroVariantsResponse getVariants(String slug, HeroLanguage language) {
        Hero currentHero = heroRepository.findBySlugAndStatus(slug, HeroStatus.READY).orElseThrow(() -> new EntityNotFoundException("Hero not found with slug: " + slug));
        Hero baseHero = resolveBaseHero(currentHero);

        HeroDetailsResponse currentHeroDetails = buildHeroDetails(currentHero, language);

        HeroVariantSummaryResponse baseHeroSummary = toVariantSummary(baseHero, language);

        List<HeroVariantSummaryResponse> costumes = heroRepository
                .findAllByBaseHeroIdAndStatusOrderByIdAsc(baseHero.getId(), HeroStatus.READY)
                .stream()
                .map(costume -> toVariantSummary(costume, language))
                .toList();

        return new HeroVariantsResponse(
                currentHeroDetails,
                baseHeroSummary,
                costumes
        );
    }

    private HeroDetailsResponse buildHeroDetails(Hero hero, HeroLanguage language) {
        List<HeroPassiveSkill> passiveLinks = heroPassiveSkillRepository.findAllByIdHeroId(hero.getId());
        List<Long> passiveIds = passiveLinks.stream()
                .map(link -> link.getId().getPassiveSkillId())
                .toList();

        List<PassiveSkill> passiveSkills = passiveIds.isEmpty()
                ? List.of()
                : passiveSkillRepository.findAllByIdIn(passiveIds);

        Long variantsRootId = hero.isCostume() ? hero.getBaseHeroId() : hero.getId();

        List<Hero> costumes = variantsRootId == null
                ? List.of()
                : heroRepository.findAllByBaseHeroIdAndStatus(variantsRootId, HeroStatus.READY);

        Element element = elementRepository.findById(hero.getElementId())
                .orElseThrow(() -> new EntityNotFoundException("Element not found"));

        Rarity rarity = rarityRepository.findById(hero.getRarityId())
                .orElseThrow(() -> new EntityNotFoundException("Rarity not found"));

        HeroClass heroClass = heroClassRepository.findById(hero.getHeroClassId())
                .orElseThrow(() -> new EntityNotFoundException("HeroClass not found"));

        ManaSpeed manaSpeed = manaSpeedRepository.findById(hero.getManaSpeedId())
                .orElseThrow(() -> new EntityNotFoundException("ManaSpeed not found"));

        Family family = hero.getFamilyId() != null
                ? familyRepository.findById(hero.getFamilyId()).orElse(null)
                : null;

        AlphaTalent alphaTalent = hero.getAlphaTalentId() != null
                ? alphaTalentRepository.findById(hero.getAlphaTalentId()).orElse(null)
                : null;

        return converter.toDetailsResponse(
                hero,
                element,
                rarity,
                heroClass,
                family,
                manaSpeed,
                alphaTalent,
                passiveSkills,
                costumes,
                language
        );
    }

    private HeroVariantSummaryResponse toVariantSummary(Hero hero, HeroLanguage language) {
        Element element = elementRepository.findById(hero.getElementId())
                .orElseThrow(() -> new EntityNotFoundException("Element not found: " + hero.getElementId()));

        Rarity rarity = rarityRepository.findById(hero.getRarityId())
                .orElseThrow(() -> new EntityNotFoundException("Rarity not found: " + hero.getRarityId()));

        return converter.toVariantSummary(hero, element, rarity, language);
    }

    private Hero resolveBaseHero(Hero hero) {
        if (!hero.isCostume()) {
            return hero;
        }

        Long baseHeroId = hero.getBaseHeroId();
        if (baseHeroId == null) {
            throw new EntityNotFoundException("Hero not found with baseHeroId: " + baseHeroId);
        }
        return heroRepository.findByIdAndStatus(baseHeroId, HeroStatus.READY).orElseThrow(() -> new EntityNotFoundException("Hero not found with baseHeroId: " + baseHeroId));
    }
}