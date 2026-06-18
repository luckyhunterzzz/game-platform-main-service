package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.CacheNames;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.*;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.dto.request.BugReportCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroBatchLookupRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroStatCalculationRequest;
import com.gameplatform.mainservice.hero.dto.response.*;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.hero.repository.projection.HeroDetailsProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroSearchProjection;
import com.gameplatform.mainservice.hero.repository.projection.HeroVariantSummaryProjection;
import com.gameplatform.mainservice.kafka.event.HeroBugReportCreatedEvent;
import com.gameplatform.mainservice.outbox.service.OutboxEventService;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.settings.service.HeroPublicVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

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
    private final HeroTagGroupRepository heroTagGroupRepository;
    private final HeroTagRepository heroTagRepository;
    private final HeroTagLinkRepository heroTagLinkRepository;
    private final BugReportRepository bugReportRepository;
    private final OutboxEventService outboxEventService;
    private final HeroStatCalculationService heroStatCalculationService;
    private final HeroPublicVisibilityService heroPublicVisibilityService;
    private final ObjectProvider<HeroPublicService> selfProvider;

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
            List<Long> alphaTalentIds,
            List<Long> roleGroupIds,
            List<Long> tagIds,
            boolean includeDrafts
    ) {
        return self().getHeroesCached(
                page,
                size,
                language,
                search,
                elementIds,
                rarityIds,
                heroClassIds,
                familyIds,
                manaSpeedIds,
                alphaTalentIds,
                roleGroupIds,
                tagIds,
                canIncludeDrafts(includeDrafts)
        );
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HEROES_PAGE)
    public HeroPageResponse getHeroesCached(
            int page,
            int size,
            HeroLanguage language,
            String search,
            List<Long> elementIds,
            List<Long> rarityIds,
            List<Long> heroClassIds,
            List<Long> familyIds,
            List<Long> manaSpeedIds,
            List<Long> alphaTalentIds,
            List<Long> roleGroupIds,
            List<Long> tagIds,
            boolean includeDraftsAuthorized
    ) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = clamp(size, 1, 50);

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize);

        List<Long> normalizedElementIds = normalizeIds(elementIds);
        List<Long> normalizedRarityIds = normalizeIds(rarityIds);
        List<Long> normalizedHeroClassIds = normalizeIds(heroClassIds);
        List<Long> normalizedFamilyIds = normalizeIds(familyIds);
        List<Long> normalizedManaSpeedIds = normalizeIds(manaSpeedIds);
        List<Long> normalizedAlphaTalentIds = normalizeIds(alphaTalentIds);
        List<Long> normalizedRoleGroupIds = normalizeIds(roleGroupIds);
        List<Long> normalizedTagIds = normalizeIds(tagIds);

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
                sqlFilterIds(normalizedRoleGroupIds),
                normalizedRoleGroupIds.isEmpty(),
                sqlFilterIds(normalizedTagIds),
                normalizedTagIds.isEmpty(),
                includeDraftsAuthorized,
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
        return self().getNamesCached(language, canIncludeDrafts(true));
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_NAMES)
    public List<HeroLookupResponse> getNamesCached(HeroLanguage language, boolean includeDraftsAuthorized) {
        String locale = language.getJsonKey();
        return converter.toLookupResponses(
                heroRepository.findAllReadyBaseHeroNames(locale, includeDraftsAuthorized)
        );
    }

    public List<HeroCardResponse> getHeroesBatch(HeroLanguage language, boolean includeDrafts, HeroBatchLookupRequest request) {
        List<Long> normalizedHeroIds = normalizeIds(request.heroIds());
        if (normalizedHeroIds.isEmpty()) {
            return List.of();
        }

        return self().getHeroesBatchCached(language, canIncludeDrafts(includeDrafts), normalizedHeroIds);
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_BATCH)
    public List<HeroCardResponse> getHeroesBatchCached(
            HeroLanguage language,
            boolean includeDraftsAuthorized,
            List<Long> normalizedHeroIds
    ) {
        return heroRepository.findHeroCardsByIds(
                        normalizedHeroIds,
                        language.getJsonKey(),
                        includeDraftsAuthorized
                )
                .stream()
                .map(converter::toCardResponse)
                .toList();
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_FILTERS)
    public HeroCatalogFiltersResponse getFilters(HeroLanguage language) {
        String locale = language.getJsonKey();

        return new HeroCatalogFiltersResponse(
                buildFilterOptions(
                        elementRepository.findAll(),
                        locale,
                        Element::getId,
                        Element::getNameJson,
                        Element::getImageBucket,
                        Element::getImageObjectKey
                ),
                buildRarityFilterOptions(locale),
                buildFilterOptions(
                        heroClassRepository.findAll(),
                        locale,
                        HeroClass::getId,
                        HeroClass::getNameJson,
                        HeroClass::getImageBucket,
                        HeroClass::getImageObjectKey
                ),
                buildFilterOptions(
                        familyRepository.findAll(),
                        locale,
                        Family::getId,
                        Family::getNameJson,
                        Family::getImageBucket,
                        Family::getImageObjectKey
                ),
                buildManaSpeedFilterOptions(locale),
                buildFilterOptions(
                        alphaTalentRepository.findAll(),
                        locale,
                        AlphaTalent::getId,
                        AlphaTalent::getNameJson,
                        AlphaTalent::getImageBucket,
                        AlphaTalent::getImageObjectKey
                ),
                buildFilterOptions(
                        heroTagGroupRepository.findAll(),
                        locale,
                        HeroTagGroup::getId,
                        HeroTagGroup::getNameJson,
                        ignored -> null,
                        ignored -> null
                ),
                buildFilterOptions(
                        heroTagRepository.findAll(),
                        locale,
                        HeroTag::getId,
                        HeroTag::getNameJson,
                        ignored -> null,
                        ignored -> null
                )
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
            String russianValue = value.ru();
            String englishValue = emptyIfNull(value.en());
            return russianValue != null && !russianValue.isBlank()
                    ? russianValue
                    : englishValue;
        }

        String englishValue = value.en();
        String russianValue = emptyIfNull(value.ru());
        return englishValue != null && !englishValue.isBlank()
                ? englishValue
                : russianValue;
    }

    public List<HeroLookupResponse> search(String query, int limit, HeroLanguage language) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        String normalizedQuery = query.trim();
        if (normalizedQuery.length() < 3) {
            return List.of();
        }

        return self().searchCached(normalizedQuery, limit, language, canIncludeDrafts(true));
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_SEARCH)
    public List<HeroLookupResponse> searchCached(
            String normalizedQuery,
            int limit,
            HeroLanguage language,
            boolean includeDraftsAuthorized
    ) {
        int normalizedLimit = clamp(limit, 1, 15);
        List<HeroSearchProjection> results = heroRepository.searchReadyBaseHeroesByName(
                normalizedQuery,
                language.getJsonKey(),
                normalizedLimit,
                includeDraftsAuthorized
        );

        return converter.toLookupResponses(results);
    }

    public HeroDetailsResponse getDetails(String slug, HeroLanguage language, boolean includeDrafts) {
        return self().getDetailsCached(slug, language, canIncludeDrafts(includeDrafts));
    }

    @Transactional
    public void createBugReport(String slug, BugReportCreateRequest request) {
        Hero hero = heroRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Hero not found with slug: " + slug));

        if (bugReportRepository.existsByHeroIdAndIsOpenTrue(hero.getId())) {
            throw new BusinessValidationException("An open bug report already exists for this hero");
        }

        OffsetDateTime createdAt = OffsetDateTime.now();
        UUID authorId = resolveCurrentUserIdOrNull();
        String authorName = request.authorName().trim();
        String description = request.description().trim();

        BugReport bugReport = BugReport.builder()
                .heroId(hero.getId())
                .authorId(authorId)
                .authorName(authorName)
                .description(description)
                .isOpen(true)
                .createdAt(createdAt)
                .build();

        BugReport savedBugReport = bugReportRepository.save(bugReport);

        HeroBugReportCreatedEvent event = new HeroBugReportCreatedEvent(
                UUID.randomUUID(),
                savedBugReport.getId(),
                hero.getId(),
                hero.getSlug(),
                resolveHeroDisplayName(hero),
                authorId,
                authorName,
                description,
                createdAt
        );

        outboxEventService.enqueueHeroBugReportCreated(savedBugReport.getId(), event);
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_DETAILS)
    public HeroDetailsResponse getDetailsCached(String slug, HeroLanguage language, boolean includeDraftsAuthorized) {
        HeroDetailsProjection currentHero = findCurrentHero(slug, language, includeDraftsAuthorized);
        return buildHeroDetails(currentHero, language.getJsonKey(), includeDraftsAuthorized);
    }

    public HeroStatCalculationResponse calculateStats(
            String slug,
            HeroLanguage language,
            boolean includeDrafts,
            HeroStatCalculationRequest request
    ) {
        HeroDetailsProjection currentHero = findCurrentHero(slug, language, canIncludeDrafts(includeDrafts));
        return heroStatCalculationService.calculate(currentHero.getId(), request);
    }

    public HeroVariantsResponse getVariants(String slug, HeroLanguage language, boolean includeDrafts) {
        return self().getVariantsCached(slug, language, canIncludeDrafts(includeDrafts));
    }

    @Cacheable(cacheNames = CacheNames.PUBLIC_HERO_VARIANTS)
    public HeroVariantsResponse getVariantsCached(String slug, HeroLanguage language, boolean includeDraftsAuthorized) {
        HeroDetailsProjection currentHero = findCurrentHero(slug, language, includeDraftsAuthorized);
        HeroVariantSummaryProjection baseHero = findBaseHero(currentHero, language, includeDraftsAuthorized);
        HeroDetailsResponse currentHeroDetails = buildHeroDetails(currentHero, language.getJsonKey(), includeDraftsAuthorized);

        return new HeroVariantsResponse(
                currentHeroDetails,
                converter.toVariantSummary(baseHero),
                buildVariantCostumes(baseHero.getId(), language, includeDraftsAuthorized)
        );
    }

    private HeroDetailsResponse buildHeroDetails(HeroDetailsProjection hero, String locale, boolean includeDraftsAuthorized) {
        Hero currentHero = heroRepository.findById(hero.getId())
                .orElseThrow(() -> new NotFoundException("Hero not found with id: " + hero.getId()));
        Hero baseHero = currentHero.isCostume()
                ? heroRepository.findById(currentHero.getBaseHeroId())
                .orElseThrow(() -> new NotFoundException("Base hero not found with id: " + currentHero.getBaseHeroId()))
                : currentHero;
        Element element = elementRepository.findById(hero.getElementId())
                .orElseThrow(() -> new NotFoundException("Element not found with id: " + hero.getElementId()));
        Rarity rarity = rarityRepository.findById(hero.getRarityId())
                .orElseThrow(() -> new NotFoundException("Rarity not found with id: " + hero.getRarityId()));
        HeroClass heroClass = heroClassRepository.findById(hero.getHeroClassId())
                .orElseThrow(() -> new NotFoundException("Hero class not found with id: " + hero.getHeroClassId()));
        Family family = hero.getFamilyId() == null
                ? null
                : familyRepository.findById(hero.getFamilyId())
                .orElseThrow(() -> new NotFoundException("Family not found with id: " + hero.getFamilyId()));
        ManaSpeed manaSpeed = manaSpeedRepository.findById(hero.getManaSpeedId())
                .orElseThrow(() -> new NotFoundException("Mana speed not found with id: " + hero.getManaSpeedId()));
        AlphaTalent alphaTalent = hero.getAlphaTalentId() == null
                ? null
                : alphaTalentRepository.findById(hero.getAlphaTalentId())
                .orElseThrow(() -> new NotFoundException("Alpha talent not found with id: " + hero.getAlphaTalentId()));
        List<Long> tagIds = heroTagLinkRepository.findAllByIdHeroId(hero.getId()).stream()
                .map(link -> link.getId().getTagId())
                .toList();
        List<HeroTag> tags = tagIds.isEmpty()
                ? List.of()
                : heroTagRepository.findAllById(tagIds);
        List<Long> roleGroupIds = tags.stream()
                .map(HeroTag::getGroupId)
                .distinct()
                .toList();
        List<HeroTagGroup> roleGroups = roleGroupIds.isEmpty()
                ? List.of()
                : heroTagGroupRepository.findAllById(roleGroupIds);
        List<PassiveSkill> passiveSkills = findPassiveSkills(hero.getId());
        List<Hero> costumes = findCostumes(hero, includeDraftsAuthorized);
        boolean hasOpenBugReport = bugReportRepository.existsByHeroIdAndIsOpenTrue(hero.getId());

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
                roleGroups.stream()
                        .map(group -> new DescribedReferenceResponse(
                                group.getId(),
                                localized(group.getNameJson(), locale),
                                localized(group.getDescriptionJson(), locale),
                                null
                        ))
                        .toList(),
                tags.stream()
                        .map(tag -> new DescribedReferenceResponse(
                                tag.getId(),
                                localized(tag.getNameJson(), locale),
                                localized(tag.getDescriptionJson(), locale),
                                null
                        ))
                        .toList(),
                passiveSkills,
                costumes,
                hasOpenBugReport,
                locale
        );
    }

    private HeroDetailsProjection findCurrentHero(String slug, HeroLanguage language, boolean includeDraftsAuthorized) {
        return heroRepository.findReadyHeroDetailsBySlug(slug, language.getJsonKey(), includeDraftsAuthorized)
                .orElseThrow(() -> new NotFoundException("Hero not found with slug: " + slug));
    }

    private HeroVariantSummaryProjection findBaseHero(HeroDetailsProjection hero, HeroLanguage language, boolean includeDraftsAuthorized) {
        if (!Boolean.TRUE.equals(hero.getIsCostume())) {
            return heroRepository.findReadyHeroVariantSummaryById(hero.getId(), language.getJsonKey(), includeDraftsAuthorized)
                    .orElseThrow(() -> new NotFoundException("Hero not found with id: " + hero.getId()));
        }

        Long baseHeroId = hero.getBaseHeroId();
        if (baseHeroId == null) {
            throw new NotFoundException("Hero not found with baseHeroId: " + baseHeroId);
        }
        return heroRepository.findReadyHeroVariantSummaryById(baseHeroId, language.getJsonKey(), includeDraftsAuthorized)
                .orElseThrow(() -> new NotFoundException("Hero not found with baseHeroId: " + baseHeroId));
    }

    private List<PassiveSkill> findPassiveSkills(Long heroId) {
        List<Long> passiveIds = heroPassiveSkillRepository.findAllByIdHeroId(heroId).stream()
                .map(link -> link.getId().getPassiveSkillId())
                .toList();

        return passiveIds.isEmpty()
                ? List.of()
                : passiveSkillRepository.findAllByIdIn(passiveIds);
    }

    private List<Hero> findCostumes(HeroDetailsProjection hero, boolean includeDraftsAuthorized) {
        Long variantsRootId = Boolean.TRUE.equals(hero.getIsCostume())
                ? hero.getBaseHeroId()
                : hero.getId();

        if (variantsRootId == null) {
            return List.of();
        }

        if (includeDraftsAuthorized) {
            return heroRepository.findAllByBaseHeroIdAndStatusIn(
                    variantsRootId,
                    List.of(HeroStatus.READY, HeroStatus.DRAFT)
            );
        }

        return heroRepository.findAllByBaseHeroIdAndStatus(variantsRootId, HeroStatus.READY);
    }

    private List<HeroVariantSummaryResponse> buildVariantCostumes(Long baseHeroId, HeroLanguage language, boolean includeDraftsAuthorized) {
        return heroRepository.findReadyHeroVariantSummariesByBaseHeroId(
                        baseHeroId,
                        language.getJsonKey(),
                        includeDraftsAuthorized
                )
                .stream()
                .map(converter::toVariantSummary)
                .toList();
    }

    private boolean canIncludeDrafts(boolean includeDraftsRequested) {
        if (heroPublicVisibilityService.isDraftVisibleInPublicCatalog()) {
            return true;
        }

        if (!includeDraftsRequested) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_superadmin".equals(authority.getAuthority()));
    }

    private UUID resolveCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String resolveHeroDisplayName(Hero hero) {
        if (hero == null) {
            return null;
        }

        LocalizedTextJson nameJson = hero.getNameJson();
        if (nameJson == null) {
            return hero.getSlug();
        }

        if (nameJson.en() != null && !nameJson.en().isBlank()) {
            return nameJson.en().trim();
        }

        if (nameJson.ru() != null && !nameJson.ru().isBlank()) {
            return nameJson.ru().trim();
        }

        return hero.getSlug();
    }

    private <T> List<HeroCatalogFilterOptionResponse> buildFilterOptions(
            List<T> items,
            String locale,
            Function<T, Long> idExtractor,
            Function<T, LocalizedTextJson> nameExtractor,
            Function<T, String> imageBucketExtractor,
            Function<T, String> imageObjectKeyExtractor
    ) {
        return items.stream()
                .map(item -> new HeroCatalogFilterOptionResponse(
                        idExtractor.apply(item),
                        localized(nameExtractor.apply(item), locale),
                        mediaUrlResolver.resolveUrl(
                                imageBucketExtractor.apply(item),
                                imageObjectKeyExtractor.apply(item)
                        )
                ))
                .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                .toList();
    }

    private List<HeroCatalogRarityFilterOptionResponse> buildRarityFilterOptions(String locale) {
        return rarityRepository.findAll().stream()
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
                .toList();
    }

    private List<HeroCatalogFilterOptionResponse> buildManaSpeedFilterOptions(String locale) {
        return manaSpeedRepository.findAll().stream()
                .map(item -> new HeroCatalogFilterOptionResponse(
                        item.getId(),
                        localized(item.getNameJson(), locale),
                        null
                ))
                .sorted((left, right) -> left.name().compareToIgnoreCase(right.name()))
                .toList();
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }

        return Math.min(value, max);
    }

    private HeroPublicService self() {
        return selfProvider.getObject();
    }
}
