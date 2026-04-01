package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkill;
import com.gameplatform.mainservice.hero.domain.entity.HeroPassiveSkillId;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.dto.response.HeroResponse;
import com.gameplatform.mainservice.hero.converter.HeroResponseConverter;
import com.gameplatform.mainservice.hero.repository.*;
import com.gameplatform.mainservice.hero.validation.HeroValidator;
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
public class HeroAdminService {

    private final Clock clock;

    private final HeroRepository heroRepository;
    private final HeroPassiveSkillRepository heroPassiveSkillRepository;

    private final HeroResponseConverter heroResponseConverter;
    private final HeroValidator heroValidator;

    public List<HeroResponse> getAll() {
        List<Hero> heroes = heroRepository.findAll();
        if (heroes.isEmpty()) {
            return List.of();
        }

        List<Long> heroIds = heroes.stream()
                .map(Hero::getId)
                .toList();

        List<HeroPassiveSkill> allLinks = heroPassiveSkillRepository.findAllByIdHeroIdIn(heroIds);
        return heroResponseConverter.toResponseList(heroes, allLinks);
    }

    public Hero getById(Long id) {
        return heroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hero not found: " + id));
    }

    public HeroResponse getResponseById(Long id) {
        return buildResponse(getById(id));
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

        syncPassiveSkills(savedHero.getId(), request.passiveSkillIds());

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

    private void applyUpsert(Hero hero, HeroUpsertRequest request, String normalizedSlug) {
        hero.setSlug(normalizedSlug);
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
        hero.setUpdatedBy(request.updatedBy());
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

    private String normalizeSlug(String slug) {
        return slug == null ? null : slug.trim().toLowerCase();
    }
}
