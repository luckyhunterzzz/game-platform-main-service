package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.response.HeroCardResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroDetailsResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSearchResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSimpleNameResponse;
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
}