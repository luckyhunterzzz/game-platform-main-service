package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.config.PublicCacheEvictionService;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.converter.HeroResponseConverter;
import com.gameplatform.mainservice.hero.domain.entity.BugReport;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkill;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkillId;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagId;
import com.gameplatform.mainservice.hero.domain.entity.HeroTagLink;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.domain.enums.HeroStatus;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroBugReportResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroBugReportsAdminResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminVariantsResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroAdminPageResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroNextCostumeIndexResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroSlugAvailabilityResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroVariantSummaryResponse;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HeroAdminService {

    private final Clock clock;

    private final HeroRepository heroRepository;
    private final HeroExpertOpinionRepository heroExpertOpinionRepository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;
    private final HeroTagLinkRepository heroTagLinkRepository;
    private final BugReportRepository bugReportRepository;

    private final HeroResponseConverter heroResponseConverter;
    private final HeroPublicResponseConverter heroPublicResponseConverter;
    private final HeroValidator heroValidator;
    private final PublicCacheEvictionService publicCacheEvictionService;

    public List<HeroResponse> getAll() {
        List<Hero> heroes = heroRepository.findAll();
        if (heroes.isEmpty()) {
            return List.of();
        }

        List<Long> heroIds = heroes.stream()
                .map(Hero::getId)
                .toList();

        List<HeroPassiveSkill> allLinks = heroPassiveSkillRepository.findAllByIdHeroIdIn(heroIds);
        List<HeroTagLink> allTagLinks = heroTagLinkRepository.findAllByIdHeroIdIn(heroIds);
        return heroResponseConverter.toResponseList(
                heroes,
                allLinks,
                allTagLinks,
                bugReportRepository::existsByHeroIdAndIsOpenTrue
        );
    }

    public HeroAdminPageResponse getCatalog(int page, int size, String search, List<Long> rarityIds, List<HeroStatus> statuses) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 50);
        List<Long> normalizedRarityIds = rarityIds == null ? List.of() : rarityIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        List<String> normalizedStatuses = statuses == null ? List.of() : statuses.stream().filter(java.util.Objects::nonNull).map(Enum::name).distinct().toList();

        PageRequest pageable = PageRequest.of(normalizedPage, normalizedSize);
        Page<Long> heroIdPage = heroRepository.findHeroIdsForAdminCatalog(
                StringUtils.hasText(search) ? search.trim() : null,
                normalizedRarityIds,
                normalizedRarityIds.isEmpty(),
                normalizedStatuses,
                normalizedStatuses.isEmpty(),
                pageable
        );

        List<Long> heroIds = heroIdPage.getContent();
        if (heroIds.isEmpty()) {
            return new HeroAdminPageResponse(
                    List.of(),
                    heroIdPage.getNumber(),
                    heroIdPage.getSize(),
                    heroIdPage.getTotalElements(),
                    heroIdPage.getTotalPages(),
                    heroIdPage.hasNext()
            );
        }

        List<Hero> heroes = heroRepository.findAllById(heroIds);
        Map<Long, Hero> heroById = new HashMap<>();
        for (Hero hero : heroes) {
            heroById.put(hero.getId(), hero);
        }

        List<HeroPassiveSkill> allLinks = heroPassiveSkillRepository.findAllByIdHeroIdIn(heroIds);
        List<HeroTagLink> allTagLinks = heroTagLinkRepository.findAllByIdHeroIdIn(heroIds);
        Map<Long, List<HeroPassiveSkill>> linksByHeroId = new HashMap<>();
        for (HeroPassiveSkill link : allLinks) {
            linksByHeroId.computeIfAbsent(link.getId().getHeroId(), ignored -> new java.util.ArrayList<>()).add(link);
        }
        Map<Long, List<HeroTagLink>> tagLinksByHeroId = new HashMap<>();
        for (HeroTagLink link : allTagLinks) {
            tagLinksByHeroId.computeIfAbsent(link.getId().getHeroId(), ignored -> new java.util.ArrayList<>()).add(link);
        }

        List<HeroResponse> items = heroIds.stream()
                .map(heroById::get)
                .filter(java.util.Objects::nonNull)
                .map(hero -> heroResponseConverter.toResponse(
                        hero,
                        linksByHeroId.getOrDefault(hero.getId(), List.of()),
                        tagLinksByHeroId.getOrDefault(hero.getId(), List.of()),
                        bugReportRepository.existsByHeroIdAndIsOpenTrue(hero.getId())
                ))
                .toList();

        return new HeroAdminPageResponse(
                items,
                heroIdPage.getNumber(),
                heroIdPage.getSize(),
                heroIdPage.getTotalElements(),
                heroIdPage.getTotalPages(),
                heroIdPage.hasNext()
        );
    }

    public Hero getById(Long id) {
        return heroRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Hero not found: " + id));
    }

    public HeroResponse getResponseById(Long id) {
        return buildResponse(getById(id));
    }

    public HeroAdminVariantsResponse getVariants(Long id, HeroLanguage language) {
        Hero currentHero = getById(id);
        String locale = language.getJsonKey();
        Long baseHeroId = currentHero.isCostume()
                ? currentHero.getBaseHeroId()
                : currentHero.getId();

        HeroVariantSummaryResponse currentHeroSummary = heroRepository.findHeroVariantSummaryById(currentHero.getId(), locale)
                .map(heroPublicResponseConverter::toVariantSummary)
                .orElseThrow(() -> new NotFoundException("Hero not found: " + currentHero.getId()));

        if (baseHeroId == null) {
            return new HeroAdminVariantsResponse(currentHeroSummary, currentHeroSummary, List.of());
        }

        HeroVariantSummaryResponse baseHeroSummary = heroRepository.findHeroVariantSummaryById(baseHeroId, locale)
                .map(heroPublicResponseConverter::toVariantSummary)
                .orElseThrow(() -> new NotFoundException("Base hero not found: " + baseHeroId));

        List<HeroVariantSummaryResponse> costumes = heroRepository.findHeroVariantSummariesByBaseHeroId(baseHeroId, locale)
                .stream()
                .map(heroPublicResponseConverter::toVariantSummary)
                .toList();

        return new HeroAdminVariantsResponse(
                currentHeroSummary,
                baseHeroSummary,
                costumes
        );
    }

    public HeroBugReportsAdminResponse getBugReports(Long heroId) {
        if (!heroRepository.existsById(heroId)) {
            throw new NotFoundException("Hero not found: " + heroId);
        }

        List<BugReport> bugReports = bugReportRepository.findAllByHeroIdOrderByCreatedAtDesc(heroId);
        HeroBugReportResponse activeReport = bugReports.stream()
                .filter(BugReport::isOpen)
                .findFirst()
                .map(this::toBugReportResponse)
                .orElse(null);

        List<HeroBugReportResponse> history = bugReports.stream()
                .filter(report -> !report.isOpen())
                .map(this::toBugReportResponse)
                .toList();

        return new HeroBugReportsAdminResponse(activeReport, history);
    }

    public HeroSlugAvailabilityResponse getSlugAvailability(String slug, Long excludeId) {
        String normalizedSlug = normalizeSlug(slug);
        if (!StringUtils.hasText(normalizedSlug)) {
            return new HeroSlugAvailabilityResponse(false);
        }

        boolean available = excludeId == null
                ? !heroRepository.existsBySlug(normalizedSlug)
                : !heroRepository.existsBySlugAndIdNot(normalizedSlug, excludeId);

        return new HeroSlugAvailabilityResponse(available);
    }

    public HeroNextCostumeIndexResponse getNextCostumeIndex(Long baseHeroId) {
        if (baseHeroId == null) {
            throw new NotFoundException("Base hero not found: null");
        }

        if (!heroRepository.existsById(baseHeroId)) {
            throw new NotFoundException("Base hero not found: " + baseHeroId);
        }

        Integer maxCostumeIndex = heroRepository.findMaxCostumeIndexByBaseHeroId(baseHeroId);
        int nextCostumeIndex = (maxCostumeIndex == null ? 0 : maxCostumeIndex) + 1;
        return new HeroNextCostumeIndexResponse(nextCostumeIndex);
    }

    @Transactional
    public Hero create(HeroUpsertRequest request) {
        String normalizedSlug = normalizeSlug(request.slug());
        heroValidator.validateCreate(request, normalizedSlug);

        OffsetDateTime now = OffsetDateTime.now(clock);

        Hero hero = Hero.builder()
                .createdAt(now)
                .updatedAt(now)
                .build();

        applyUpsert(hero, request, normalizedSlug);

        Hero savedHero = heroRepository.save(hero);
        syncBugReportState(savedHero.getId(), request, savedHero.getUpdatedBy());

        syncPassiveSkills(savedHero.getId(), request.passiveSkillIds());
        syncTags(savedHero.getId(), request.tagIds());
        publicCacheEvictionService.evictHeroCaches();

        return savedHero;
    }

    @Transactional
    public Hero update(Long id, HeroUpsertRequest request) {
        Hero hero = getById(id);
        String normalizedSlug = normalizeSlug(request.slug());

        heroValidator.validateUpdate(id, request, normalizedSlug);

        hero.setUpdatedAt(OffsetDateTime.now(clock));
        applyUpsert(hero, request, normalizedSlug);

        Hero savedHero = heroRepository.save(hero);

        syncPassiveSkills(savedHero.getId(), request.passiveSkillIds());
        syncTags(savedHero.getId(), request.tagIds());
        publicCacheEvictionService.evictHeroCaches();

        return savedHero;
    }

    @Transactional
    public void delete(Long id) {
        if (!heroRepository.existsById(id)) {
            throw new NotFoundException("Hero not found: " + id);
        }

        heroExpertOpinionRepository.deleteAllByHeroId(id);
        heroPassiveSkillRepository.deleteAllByIdHeroId(id);
        heroTagLinkRepository.deleteAllByIdHeroId(id);
        heroRepository.deleteById(id);
        publicCacheEvictionService.evictHeroCaches();
    }

    public HeroResponse buildResponse(Hero hero) {
        List<HeroPassiveSkill> passiveLinks = heroPassiveSkillRepository.findAllByIdHeroId(hero.getId());
        List<HeroTagLink> tagLinks = heroTagLinkRepository.findAllByIdHeroId(hero.getId());
        return heroResponseConverter.toResponse(
                hero,
                passiveLinks,
                tagLinks,
                bugReportRepository.existsByHeroIdAndIsOpenTrue(hero.getId())
        );
    }

    private void applyUpsert(Hero hero, HeroUpsertRequest request, String normalizedSlug) {
        hero.setSlug(normalizedSlug);
        hero.setNameJson(request.nameJson());
        hero.setSpecialSkillNameJson(request.specialSkillNameJson());
        hero.setSpecialSkillDescriptionJson(request.specialSkillDescriptionJson());
        hero.setBaseAttack(request.baseAttack());
        hero.setBaseArmor(request.baseArmor());
        hero.setBaseHp(request.baseHp());
        hero.setBasePower(request.basePower());
        hero.setElementId(request.elementId());
        hero.setRarityId(request.rarityId());
        hero.setHeroClassId(request.heroClassId());
        hero.setFamilyId(request.familyId());
        hero.setManaSpeedId(request.manaSpeedId());
        hero.setAlphaTalentId(request.alphaTalentId());
        hero.setImageBucketJson(request.imageBucketJson());
        hero.setImageObjectKeyJson(request.imageObjectKeyJson());
        hero.setPreviewBucket(request.previewBucket());
        hero.setPreviewObjectKey(request.previewObjectKey());
        hero.setCostume(request.isCostume());
        hero.setBaseHeroId(request.baseHeroId());
        hero.setCostumeIndex(request.costumeIndex());
        hero.setCostumeBonusJson(request.costumeBonusJson());
        hero.setReleaseDate(request.releaseDate());
        hero.setStatus(request.status());
        hero.setUpdatedBy(request.updatedBy());
        hero.setUpdatedByEmail(resolveUpdatedByEmail(request));
    }

    private void syncBugReportState(Long heroId, HeroUpsertRequest request, String closedBy) {
        if (request.hasOpenBugReport() == null) {
            return;
        }

        boolean currentHasOpenBugReport = bugReportRepository.existsByHeroIdAndIsOpenTrue(heroId);
        boolean requestedHasOpenBugReport = request.hasOpenBugReport();

        if (currentHasOpenBugReport == requestedHasOpenBugReport) {
            return;
        }

        if (!currentHasOpenBugReport && requestedHasOpenBugReport) {
            throw new BusinessValidationException("Open bug report cannot be created from hero admin update");
        }

        BugReport openBugReport = bugReportRepository.findByHeroIdAndIsOpenTrue(heroId)
                .orElseThrow(() -> new NotFoundException("Open bug report not found for hero: " + heroId));

        openBugReport.setOpen(false);
        openBugReport.setClosedAt(OffsetDateTime.now(clock));
        openBugReport.setClosedBy(closedBy);
        bugReportRepository.save(openBugReport);
    }

    private HeroBugReportResponse toBugReportResponse(BugReport bugReport) {
        return new HeroBugReportResponse(
                bugReport.getId(),
                bugReport.getHeroId(),
                bugReport.getAuthorId(),
                bugReport.getAuthorName(),
                bugReport.getDescription(),
                bugReport.isOpen(),
                bugReport.getCreatedAt(),
                bugReport.getClosedAt(),
                bugReport.getClosedBy()
        );
    }

    private void syncPassiveSkills(Long heroId, List<Long> passiveSkillIds) {
        heroPassiveSkillRepository.deleteAllByIdHeroId(heroId);

        if (passiveSkillIds == null || passiveSkillIds.isEmpty()) {
            return;
        }

        Set<Long> uniquePassiveSkillIds = new LinkedHashSet<>(passiveSkillIds);

        List<HeroPassiveSkill> links = uniquePassiveSkillIds.stream()
                .map(passiveSkillId -> HeroPassiveSkill.builder()
                        .id(new HeroPassiveSkillId(heroId, passiveSkillId))
                        .build())
                .toList();

        heroPassiveSkillRepository.saveAll(links);
    }

    private void syncTags(Long heroId, List<Long> tagIds) {
        heroTagLinkRepository.deleteAllByIdHeroId(heroId);

        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        Set<Long> uniqueTagIds = new LinkedHashSet<>(tagIds);

        List<HeroTagLink> links = uniqueTagIds.stream()
                .map(tagId -> HeroTagLink.builder()
                        .id(new HeroTagId(heroId, tagId))
                        .build())
                .toList();

        heroTagLinkRepository.saveAll(links);
    }

    private String normalizeSlug(String slug) {
        return slug == null ? null : slug.trim().toLowerCase();
    }

    private String resolveUpdatedByEmail(HeroUpsertRequest request) {
        if (request.updatedByEmail() != null && !request.updatedByEmail().isBlank()) {
            return request.updatedByEmail().trim();
        }

        if (request.updatedBy() != null && request.updatedBy().contains("@")) {
            return request.updatedBy().trim();
        }

        return null;
    }
}
