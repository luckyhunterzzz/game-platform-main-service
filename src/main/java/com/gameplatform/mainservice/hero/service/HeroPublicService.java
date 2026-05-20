package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.request.HeroBatchLookupRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.hero.repository.projection.HeroDetailsProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroVariantSummaryProjection;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
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
    private final ElementRepository elementRepository;
    private final RarityRepository rarityRepository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;
    private final PassiveSkillRepository passiveSkillRepository;
    private final HeroClassRepository heroClassRepository;
    private final FamilyRepository familyRepository;
    private final ManaSpeedRepository manaSpeedRepository;
    private final AlphaTalentRepository alphaTalentRepository;
    private final HeroStatCalculationService heroStatCalculationService;

    private final HeroPublicResponseConverter converter;
    private final MediaUrlResolver mediaUrlResolver;

    public HeroPageResponse getHeroes(
            int page,
            int size,
            HeroLanguage language,
            String search,
            List<Long> elementIds,
            List<Long> rarityIds,
            List<Long> heroClassIds,
            List<Long> familyIds,
            List<Long> manaSpeedIds,
            List<Long> alphaTalentIds
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 50);

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize);

        List<Long> normalizedElementIds = normalizeIds(elementIds);
        List<Long> normalizedRarityIds = normalizeIds(rarityIds);
        List<Long> normalizedHeroClassIds = normalizeIds(heroClassIds);
        List<Long> normalizedFamilyIds = normalizeIds(familyIds);
        List<Long> normalizedManaSpeedIds = normalizeIds(manaSpeedIds);
        List<Long> normalizedAlphaTalentIds = normalizeIds(alphaTalentIds);

        Page<HeroCardResponse> heroPage = heroRepository.findReadyHeroCards(
                language.getJsonKey(),
                StringUtils.hasText(search) ? search.trim() : null,
                sqlFilterIds(normalizedElementIds),
                normalizedElementIds.isEmpty(),
                sqlFilterIds(normalizedRarityIds),
                normalizedRarityIds.isEmpty(),
                sqlFilterIds(normalizedHeroClassIds),
                normalizedHeroClassIds.isEmpty(),
                sqlFilterIds(normalizedFamilyIds),
                normalizedFamilyIds.isEmpty(),
                sqlFilterIds(normalizedManaSpeedIds),
                normalizedManaSpeedIds.isEmpty(),
                sqlFilterIds(normalizedAlphaTalentIds),
                normalizedAlphaTalentIds.isEmpty(),
                pageable
        ).map(converter::toCardResponse);

        return new HeroPageResponse(
                heroPage.getContent(),
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

    public List<HeroCardResponse> getHeroesBatch(HeroLanguage language, HeroBatchLookupRequest request) {
        List<Long> normalizedHeroIds = normalizeIds(request.heroIds());
        if (normalizedHeroIds.isEmpty()) {
            return List.of();
        }

        return heroRepository.findReadyHeroCardsByIds(normalizedHeroIds, language.getJsonKey())
                .stream()
                .map(converter::toCardResponse)
                .toList();
    }

    public HeroCatalogFiltersResponse getFilters(HeroLanguage language) {
        String locale = language.getJsonKey();

        return new HeroCatalogFiltersResponse(
                elementRepository.findAll().stream()
                        .map(item -> new HeroCatalogFilterOptionResponse(
                                item.getId(),
                                localized(item.getNameJson(), locale),
                                mediaUrlResolver.resolveUrl(item.getImageBucket(), item.getImageObjectKey())
                        ))
                        .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                        .toList(),
                rarityRepository.findAll().stream()
                        .map(item -> new HeroCatalogRarityFilterOptionResponse(
                                item.getId(),
                                localized(item.getNameJson(), locale),
                                item.getStars(),
                                mediaUrlResolver.resolveUrl(item.getImageBucket(), item.getImageObjectKey())
                        ))
                        .sorted((left, right) -> {
                            int starsCompare = Integer.compare(left.stars(), right.stars());
                            return starsCompare != 0 ? starsCompare : left.name().compareToIgnoreCase(right.name());
                        })
                        .toList(),
                heroClassRepository.findAll().stream()
                        .map(item -> new HeroCatalogFilterOptionResponse(
                                item.getId(),
                                localized(item.getNameJson(), locale),
                                mediaUrlResolver.resolveUrl(item.getImageBucket(), item.getImageObjectKey())
                        ))
                        .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                        .toList(),
                familyRepository.findAll().stream()
                        .map(item -> new HeroCatalogFilterOptionResponse(
                                item.getId(),
                                localized(item.getNameJson(), locale),
                                mediaUrlResolver.resolveUrl(item.getImageBucket(), item.getImageObjectKey())
                        ))
                        .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                        .toList(),
                manaSpeedRepository.findAll().stream()
                        .map(item -> new HeroCatalogFilterOptionResponse(item.getId(), localized(item.getNameJson(), locale), null))
                        .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                        .toList(),
                alphaTalentRepository.findAll().stream()
                        .map(item -> new HeroCatalogFilterOptionResponse(
                                item.getId(),
                                localized(item.getNameJson(), locale),
                                mediaUrlResolver.resolveUrl(item.getImageBucket(), item.getImageObjectKey())
                        ))
                        .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                        .toList()
        );
    }

    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null) {
            return List.of();
        }

        return ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();
    }

    private List<Long> sqlFilterIds(List<Long> ids) {
        return ids.isEmpty() ? List.of(-1L) : ids;
    }

    private String localized(LocalizedTextJson value, String locale) {
        if (value == null) {
            return "";
        }

        if ("ru".equalsIgnoreCase(locale)) {
            return value.ru() != null && !value.ru().isBlank()
                    ? value.ru()
                    : value.en() == null ? "" : value.en();
        }

        return value.en() != null && !value.en().isBlank()
                ? value.en()
                : value.ru() == null ? "" : value.ru();
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
        HeroDetailsProjection currentHero = findCurrentHero(slug, language);
        return buildHeroDetails(currentHero, language.getJsonKey());
    }

    public HeroStatCalculationResponse calculateStats(
            String slug,
            HeroLanguage language,
            HeroStatCalculationRequest request
    ) {
        HeroDetailsProjection currentHero = findCurrentHero(slug, language);
        return heroStatCalculationService.calculate(currentHero.getId(), request);
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
        Hero currentHero = heroRepository.findById(hero.getId())
                .orElseThrow(() -> new EntityNotFoundException("Hero not found with id: " + hero.getId()));
        Hero baseHero = currentHero.isCostume()
                ? heroRepository.findById(currentHero.getBaseHeroId())
                .orElseThrow(() -> new EntityNotFoundException("Base hero not found with id: " + currentHero.getBaseHeroId()))
                : currentHero;
        Element element = elementRepository.findById(hero.getElementId())
                .orElseThrow(() -> new EntityNotFoundException("Element not found with id: " + hero.getElementId()));
        Rarity rarity = rarityRepository.findById(hero.getRarityId())
                .orElseThrow(() -> new EntityNotFoundException("Rarity not found with id: " + hero.getRarityId()));
        HeroClass heroClass = heroClassRepository.findById(hero.getHeroClassId())
                .orElseThrow(() -> new EntityNotFoundException("Hero class not found with id: " + hero.getHeroClassId()));
        Family family = hero.getFamilyId() == null
                ? null
                : familyRepository.findById(hero.getFamilyId())
                .orElseThrow(() -> new EntityNotFoundException("Family not found with id: " + hero.getFamilyId()));
        ManaSpeed manaSpeed = manaSpeedRepository.findById(hero.getManaSpeedId())
                .orElseThrow(() -> new EntityNotFoundException("Mana speed not found with id: " + hero.getManaSpeedId()));
        AlphaTalent alphaTalent = hero.getAlphaTalentId() == null
                ? null
                : alphaTalentRepository.findById(hero.getAlphaTalentId())
                .orElseThrow(() -> new EntityNotFoundException("Alpha talent not found with id: " + hero.getAlphaTalentId()));
        List<PassiveSkill> passiveSkills = findPassiveSkills(hero.getId());
        List<Hero> costumes = findCostumes(hero);

        return converter.toDetailsResponse(
                hero,
                currentHero,
                baseHero,
                element,
                rarity,
                heroClass,
                family,
                manaSpeed,
                alphaTalent,
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
