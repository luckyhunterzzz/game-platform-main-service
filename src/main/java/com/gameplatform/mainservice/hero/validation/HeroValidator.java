package com.gameplatform.mainservice.hero.validation;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.projection.HeroReferenceValidationProjection;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HeroValidator {

    private final HeroRepository heroRepository;
    private final ImageReferenceValidator imageReferenceValidator;

    public void validateCreate(HeroUpsertRequest request, String normalizedSlug) {
        if (heroRepository.existsBySlug(normalizedSlug)) {
            throw new IllegalStateException("Hero with slug already exists: " + normalizedSlug);
        }

        validateCommon(request);
    }

    public void validateUpdate(Long heroId, HeroUpsertRequest request, String normalizedSlug) {
        heroRepository.findBySlug(normalizedSlug)
                .filter(existing -> !existing.getId().equals(heroId))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Hero with slug already exists: " + normalizedSlug);
                });

        if (request.baseHeroId() != null && request.baseHeroId().equals(heroId)) {
            throw new IllegalStateException("Hero cannot reference itself as base hero");
        }

        validateCommon(request);
    }

    private void validateCommon(HeroUpsertRequest request) {
        imageReferenceValidator.validate(request.imageBucketJson() != null ? request.imageBucketJson().ru() : null,
                request.imageObjectKeyJson() != null ? request.imageObjectKeyJson().ru() : null);
        imageReferenceValidator.validate(request.imageBucketJson() != null ? request.imageBucketJson().en() : null,
                request.imageObjectKeyJson() != null ? request.imageObjectKeyJson().en() : null);

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
        HeroReferenceValidationProjection validation = heroRepository.validateReferences(
                elementId,
                rarityId,
                heroClassId,
                familyId,
                manaSpeedId,
                alphaTalentId,
                baseHeroId
        );

        if (!Boolean.TRUE.equals(validation.getElementExists())) {
            throw new EntityNotFoundException("Element not found: " + elementId);
        }

        if (!Boolean.TRUE.equals(validation.getRarityExists())) {
            throw new EntityNotFoundException("Rarity not found: " + rarityId);
        }

        if (!Boolean.TRUE.equals(validation.getHeroClassExists())) {
            throw new EntityNotFoundException("HeroClass not found: " + heroClassId);
        }

        if (!Boolean.TRUE.equals(validation.getFamilyExists())) {
            throw new EntityNotFoundException("Family not found: " + familyId);
        }

        if (!Boolean.TRUE.equals(validation.getManaSpeedExists())) {
            throw new EntityNotFoundException("ManaSpeed not found: " + manaSpeedId);
        }

        if (!Boolean.TRUE.equals(validation.getAlphaTalentExists())) {
            throw new EntityNotFoundException("AlphaTalent not found: " + alphaTalentId);
        }

        if (!Boolean.TRUE.equals(validation.getBaseHeroExists())) {
            throw new EntityNotFoundException("Base hero not found: " + baseHeroId);
        }

        if (passiveSkillIds != null && !passiveSkillIds.isEmpty()) {
            List<Long> uniquePassiveSkillIds = List.copyOf(new LinkedHashSet<>(passiveSkillIds));
            Set<Long> existingPassiveSkillIds = Set.copyOf(
                    heroRepository.findExistingPassiveSkillIds(uniquePassiveSkillIds)
            );

            for (Long passiveSkillId : uniquePassiveSkillIds) {
                if (!existingPassiveSkillIds.contains(passiveSkillId)) {
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
}
