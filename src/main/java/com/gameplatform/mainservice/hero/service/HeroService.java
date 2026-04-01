package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkill;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkillId;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.request.HeroCreateRequest;
import com.gameplatform.mainservice.hero.dto.request.HeroUpdateRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.converter.HeroResponseConverter;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HeroService {

    private final Clock clock;

    private final HeroRepository heroRepository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;

    private final ElementRepository elementRepository;
    private final RarityRepository rarityRepository;
    private final HeroClassRepository heroClassRepository;
    private final FamilyRepository familyRepository;
    private final ManaSpeedRepository manaSpeedRepository;
    private final AlphaTalentRepository alphaTalentRepository;
    private final PassiveSkillRepository passiveSkillRepository;

    private final HeroResponseConverter heroResponseConverter;
    private final ImageReferenceValidator imageReferenceValidator;

    public List<Hero> getAll() {
        return heroRepository.findAll();
    }

    public Hero getById(Long id) {
        return heroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hero not found: " + id));
    }

    @Transactional
    public Hero create(HeroCreateRequest request) {
        validateCreateRequest(request);

        OffsetDateTime now = OffsetDateTime.now(clock);

        Hero hero = Hero.builder()
                .slug(normalizeSlug(request.slug()))
                .nameJson(request.nameJson())
                .specialSkillNameJson(request.specialSkillNameJson())
                .specialSkillDescriptionJson(request.specialSkillDescriptionJson())
                .baseAttack(request.baseAttack())
                .baseArmor(request.baseArmor())
                .baseHp(request.baseHp())
                .elementId(request.elementId())
                .rarityId(request.rarityId())
                .heroClassId(request.heroClassId())
                .familyId(request.familyId())
                .manaSpeedId(request.manaSpeedId())
                .alphaTalentId(request.alphaTalentId())
                .imageBucket(request.imageBucket())
                .imageObjectKey(request.imageObjectKey())
                .isCostume(request.isCostume())
                .baseHeroId(request.baseHeroId())
                .costumeBonusJson(request.costumeBonusJson())
                .releaseDate(request.releaseDate())
                .status(request.status())
                .createdAt(now)
                .updatedAt(now)
                .updatedBy(request.updatedBy())
                .build();

        Hero savedHero = heroRepository.save(hero);

        replacePassiveSkills(savedHero.getId(), request.passiveSkillIds());

        return savedHero;
    }

    @Transactional
    public Hero update(Long id, HeroUpdateRequest request) {
        Hero hero = getById(id);

        validateUpdateRequest(id, request);

        hero.setSlug(normalizeSlug(request.slug()));
        hero.setNameJson(request.nameJson());
        hero.setSpecialSkillNameJson(request.specialSkillNameJson());
        hero.setSpecialSkillDescriptionJson(request.specialSkillDescriptionJson());
        hero.setBaseAttack(request.baseAttack());
        hero.setBaseArmor(request.baseArmor());
        hero.setBaseHp(request.baseHp());
        hero.setElementId(request.elementId());
        hero.setRarityId(request.rarityId());
        hero.setHeroClassId(request.heroClassId());
        hero.setFamilyId(request.familyId());
        hero.setManaSpeedId(request.manaSpeedId());
        hero.setAlphaTalentId(request.alphaTalentId());
        hero.setImageBucket(request.imageBucket());
        hero.setImageObjectKey(request.imageObjectKey());
        hero.setCostume(request.isCostume());
        hero.setBaseHeroId(request.baseHeroId());
        hero.setCostumeBonusJson(request.costumeBonusJson());
        hero.setReleaseDate(request.releaseDate());
        hero.setStatus(request.status());
        hero.setUpdatedAt(OffsetDateTime.now(clock));
        hero.setUpdatedBy(request.updatedBy());

        Hero savedHero = heroRepository.save(hero);

        replacePassiveSkills(savedHero.getId(), request.passiveSkillIds());

        return savedHero;
    }

    @Transactional
    public void delete(Long id) {
        if (!heroRepository.existsById(id)) {
            throw new EntityNotFoundException("Hero not found: " + id);
        }

        heroPassiveSkillRepository.deleteAllByIdHeroId(id);
        heroRepository.deleteById(id);
    }

    public HeroResponse buildResponse(Hero hero) {
        List<HeroPassiveSkill> links = heroPassiveSkillRepository.findAllByIdHeroId(hero.getId());
        return heroResponseConverter.toResponse(hero, links);
    }

    private void validateCreateRequest(HeroCreateRequest request) {
        String slug = normalizeSlug(request.slug());

        if (heroRepository.existsBySlug(slug)) {
            throw new IllegalStateException("Hero with slug already exists: " + slug);
        }

        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        validateReferences(
                request.elementId(),
                request.rarityId(),
                request.heroClassId(),
                request.familyId(),
                request.manaSpeedId(),
                request.alphaTalentId(),
                request.baseHeroId(),
                request.passiveSkillIds()
        );

        validateCostumeFields(request.isCostume(), request.baseHeroId(), request.costumeBonusJson());
    }

    private void validateUpdateRequest(Long heroId, HeroUpdateRequest request) {
        String slug = normalizeSlug(request.slug());

        heroRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(heroId))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Hero with slug already exists: " + slug);
                });

        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());

        if (request.baseHeroId() != null && request.baseHeroId().equals(heroId)) {
            throw new IllegalStateException("Hero cannot reference itself as base hero");
        }

        validateReferences(
                request.elementId(),
                request.rarityId(),
                request.heroClassId(),
                request.familyId(),
                request.manaSpeedId(),
                request.alphaTalentId(),
                request.baseHeroId(),
                request.passiveSkillIds()
        );

        validateCostumeFields(request.isCostume(), request.baseHeroId(), request.costumeBonusJson());
    }

    private void validateReferences(
            Long elementId,
            Long rarityId,
            Long heroClassId,
            Long familyId,
            Long manaSpeedId,
            Long alphaTalentId,
            Long baseHeroId,
            List<Long> passiveSkillIds
    ) {
        if (!elementRepository.existsById(elementId)) {
            throw new EntityNotFoundException("Element not found: " + elementId);
        }

        if (!rarityRepository.existsById(rarityId)) {
            throw new EntityNotFoundException("Rarity not found: " + rarityId);
        }

        if (!heroClassRepository.existsById(heroClassId)) {
            throw new EntityNotFoundException("HeroClass not found: " + heroClassId);
        }

        if (familyId != null && !familyRepository.existsById(familyId)) {
            throw new EntityNotFoundException("Family not found: " + familyId);
        }

        if (!manaSpeedRepository.existsById(manaSpeedId)) {
            throw new EntityNotFoundException("ManaSpeed not found: " + manaSpeedId);
        }

        if (alphaTalentId != null && !alphaTalentRepository.existsById(alphaTalentId)) {
            throw new EntityNotFoundException("AlphaTalent not found: " + alphaTalentId);
        }

        if (baseHeroId != null && !heroRepository.existsById(baseHeroId)) {
            throw new EntityNotFoundException("Base hero not found: " + baseHeroId);
        }

        if (passiveSkillIds != null) {
            for (Long passiveSkillId : new LinkedHashSet<>(passiveSkillIds)) {
                if (!passiveSkillRepository.existsById(passiveSkillId)) {
                    throw new EntityNotFoundException("PassiveSkill not found: " + passiveSkillId);
                }
            }
        }
    }

    private void validateCostumeFields(Boolean isCostume, Long baseHeroId, CostumeBonusJson costumeBonusJson) {
        if (Boolean.TRUE.equals(isCostume) && baseHeroId == null) {
            throw new BusinessValidationException("Costume hero must have baseHeroId");
        }

        if (Boolean.FALSE.equals(isCostume) && baseHeroId != null) {
            throw new BusinessValidationException("Base hero cannot have baseHeroId");
        }

        if (Boolean.FALSE.equals(isCostume) && costumeBonusJson != null) {
            throw new BusinessValidationException("Base hero cannot have costumeBonusJson");
        }
    }

    private void replacePassiveSkills(Long heroId, List<Long> passiveSkillIds) {
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

    private String normalizeSlug(String slug) {
        return slug == null ? null : slug.trim().toLowerCase();
    }
}
