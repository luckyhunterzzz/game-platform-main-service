package com.gameplatform.mainservice.hero.validation;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.dto.json.CostumeBonusJson;
import com.gameplatform.mainservice.hero.dto.request.HeroUpsertRequest;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.HeroTagRepository;
import com.gameplatform.mainservice.hero.repository.projection.HeroReferenceValidationProjection;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.function.BooleanSupplier;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class HeroValidator {

    private final HeroRepository heroRepository;
    private final HeroTagRepository heroTagRepository;
    private final ImageReferenceValidator imageReferenceValidator;

    public void validateCreate(HeroUpsertRequest request, String normalizedSlug) {
        if (heroRepository.existsBySlug(normalizedSlug)) {
            throw new BusinessValidationException("Hero with slug already exists: " + normalizedSlug);
        }

        validateCommon(null, request);
    }

    public void validateUpdate(Long heroId, HeroUpsertRequest request, String normalizedSlug) {
        heroRepository.findBySlug(normalizedSlug)
                .filter(existing -> !existing.getId().equals(heroId))
                .ifPresent(existing -> {
                    throw new BusinessValidationException("Hero with slug already exists: " + normalizedSlug);
                });

        if (request.baseHeroId() != null && request.baseHeroId().equals(heroId)) {
            throw new BusinessValidationException("Hero cannot reference itself as base hero");
        }

        validateCommon(heroId, request);
    }

    public String normalizeDictionaryName(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.toLowerCase(Locale.ROOT);
    }

    public void validateDuplicateDictionaryName(String dictionaryName, BooleanSupplier duplicateExistsSupplier) {
        if (duplicateExistsSupplier.getAsBoolean()) {
            throw new BusinessValidationException(dictionaryName + " with same RU or EN name already exists");
        }
    }

    private void validateCommon(Long heroId, HeroUpsertRequest request) {
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
                request.passiveSkillIds(),
                request.tagIds()
        );

        validateCostumeFields(heroId, request.isCostume(), request.baseHeroId(), request.costumeIndex(), request.costumeBonusJson());
    }

    private void validateReferences(
            Long elementId,
            Long rarityId,
            Long heroClassId,
            Long familyId,
            Long manaSpeedId,
            Long alphaTalentId,
            Long baseHeroId,
            List<Long> passiveSkillIds,
            List<Long> tagIds
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
            throw new NotFoundException("Element not found: " + elementId);
        }

        if (!Boolean.TRUE.equals(validation.getRarityExists())) {
            throw new NotFoundException("Rarity not found: " + rarityId);
        }

        if (!Boolean.TRUE.equals(validation.getHeroClassExists())) {
            throw new NotFoundException("HeroClass not found: " + heroClassId);
        }

        if (!Boolean.TRUE.equals(validation.getFamilyExists())) {
            throw new NotFoundException("Family not found: " + familyId);
        }

        if (!Boolean.TRUE.equals(validation.getManaSpeedExists())) {
            throw new NotFoundException("ManaSpeed not found: " + manaSpeedId);
        }

        if (!Boolean.TRUE.equals(validation.getAlphaTalentExists())) {
            throw new NotFoundException("AlphaTalent not found: " + alphaTalentId);
        }

        if (!Boolean.TRUE.equals(validation.getBaseHeroExists())) {
            throw new NotFoundException("Base hero not found: " + baseHeroId);
        }

        if (passiveSkillIds != null && !passiveSkillIds.isEmpty()) {
            List<Long> uniquePassiveSkillIds = List.copyOf(new LinkedHashSet<>(passiveSkillIds));
            Set<Long> existingPassiveSkillIds = Set.copyOf(
                    heroRepository.findExistingPassiveSkillIds(uniquePassiveSkillIds)
            );

            for (Long passiveSkillId : uniquePassiveSkillIds) {
                if (!existingPassiveSkillIds.contains(passiveSkillId)) {
                    throw new NotFoundException("PassiveSkill not found: " + passiveSkillId);
                }
            }
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            List<Long> uniqueTagIds = List.copyOf(new LinkedHashSet<>(tagIds));
            Set<Long> existingTagIds = Set.copyOf(
                    heroTagRepository.findAllById(uniqueTagIds).stream()
                            .map(tag -> tag.getId())
                            .toList()
            );

            for (Long tagId : uniqueTagIds) {
                if (!existingTagIds.contains(tagId)) {
                    throw new NotFoundException("HeroTag not found: " + tagId);
                }
            }
        }
    }

    private void validateCostumeFields(
            Long heroId,
            Boolean isCostume,
            Long baseHeroId,
            Integer costumeIndex,
            CostumeBonusJson costumeBonusJson
    ) {
        if (Boolean.TRUE.equals(isCostume) && baseHeroId == null) {
            throw new BusinessValidationException("Costume hero must have baseHeroId");
        }

        if (Boolean.TRUE.equals(isCostume) && costumeIndex == null) {
            throw new BusinessValidationException("Costume hero must have costumeIndex");
        }

        if (Boolean.TRUE.equals(isCostume) && costumeBonusJson == null) {
            throw new BusinessValidationException("Costume hero must have costumeBonusJson");
        }

        if (Boolean.FALSE.equals(isCostume) && baseHeroId != null) {
            throw new BusinessValidationException("Base hero cannot have baseHeroId");
        }

        if (Boolean.FALSE.equals(isCostume) && costumeIndex != null) {
            throw new BusinessValidationException("Base hero cannot have costumeIndex");
        }

        if (Boolean.FALSE.equals(isCostume) && costumeBonusJson != null) {
            throw new BusinessValidationException("Base hero cannot have costumeBonusJson");
        }

        if (Boolean.TRUE.equals(isCostume)) {
            heroRepository.findById(baseHeroId)
                    .ifPresentOrElse(baseHero -> {
                        if (baseHero.isCostume()) {
                            throw new BusinessValidationException("Costume hero must reference a base hero");
                        }
                    }, () -> {
                        throw new NotFoundException("Base hero not found: " + baseHeroId);
                    });

            heroRepository.findByBaseHeroIdAndCostumeIndex(baseHeroId, costumeIndex)
                    .filter(existing -> heroId == null || !existing.getId().equals(heroId))
                    .ifPresent(existing -> {
                        throw new BusinessValidationException("Costume index already exists for base hero");
                    });
        }
    }
}


