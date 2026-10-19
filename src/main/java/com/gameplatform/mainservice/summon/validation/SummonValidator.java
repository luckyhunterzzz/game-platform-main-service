package com.gameplatform.mainservice.summon.validation;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusConditionRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusRuleCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusStepCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonVariantCloneRequest;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SummonValidator {

    private static final Pattern SLUG_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    private final SummonRepository summonRepository;
    private final SummonProbabilityDefinitionRepository probabilityDefinitionRepository;
    private final HeroRepository heroRepository;
    private final RarityRepository rarityRepository;
    private final ImageReferenceValidator imageReferenceValidator;

    public void validateCreate(SummonCreateRequest request) {
        validateUpsert(request, null);
    }

    public void validateUpdate(UUID summonId, SummonCreateRequest request) {
        validateUpsert(request, summonId);
    }

    private void validateUpsert(SummonCreateRequest request, UUID summonId) {
        validateSlug(request.slug(), summonId);
        validateLocalizedText(request.nameJson(), "nameJson");
        imageReferenceValidator.validate(request.imageBucket(), request.imageObjectKey());
        imageReferenceValidator.validate(request.backgroundImageBucket(), request.backgroundImageObjectKey());

        List<SummonProbabilityCreateRequest> probabilities = safeList(request.probabilities());
        List<SummonBonusRuleCreateRequest> bonusRules = safeList(request.bonusRules());

        validateProbabilities(probabilities, request.status());
        validateBonusRules(bonusRules, probabilities);
    }

    public void validateVariantClone(SummonVariantCloneRequest request) {
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new BusinessValidationException("endsAt must be after startsAt");
        }
    }

    public void validateVariantStatus(ContentStatus status, List<SummonProbability> probabilities) {
        if (status != ContentStatus.READY) {
            return;
        }

        BigDecimal mainTotal = probabilities.stream()
                .filter(probability -> probability.getKind() == SummonProbabilityKind.MAIN)
                .map(SummonProbability::getValuePercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (mainTotal.compareTo(ONE_HUNDRED) != 0) {
            throw new BusinessValidationException("MAIN probabilities must total 100% for a READY summon variant");
        }
    }

    private void validateSlug(String slug, UUID summonId) {
        String normalizedSlug = slug.trim().toLowerCase(Locale.ROOT);

        if (!SLUG_PATTERN.matcher(normalizedSlug).matches()) {
            throw new BusinessValidationException("slug must contain only lowercase letters, numbers, and hyphens");
        }
        boolean slugExists = summonId == null
                ? summonRepository.existsBySlug(normalizedSlug)
                : summonRepository.existsBySlugAndIdNot(normalizedSlug, summonId);
        if (slugExists) {
            throw new BusinessValidationException("Summon slug already exists: " + normalizedSlug);
        }
    }

    private void validateProbabilities(
            List<SummonProbabilityCreateRequest> probabilities,
            ContentStatus status
    ) {
        Set<UUID> definitionIds = new HashSet<>();
        Set<Integer> positions = new HashSet<>();
        Set<Long> heroIds = new HashSet<>();

        for (SummonProbabilityCreateRequest probability : probabilities) {
            if (!definitionIds.add(probability.definitionId())) {
                throw new BusinessValidationException("probability definitions must not contain duplicates");
            }
            if (!positions.add(probability.position())) {
                throw new BusinessValidationException("probability positions must not contain duplicates");
            }
            heroIds.addAll(safeList(probability.heroIds()));
        }

        long existingDefinitionCount = probabilityDefinitionRepository.findAllById(definitionIds).size();
        if (existingDefinitionCount != definitionIds.size()) {
            throw new BusinessValidationException("one or more probability definitions do not exist");
        }

        long existingHeroCount = heroRepository.findAllById(heroIds).size();
        if (existingHeroCount != heroIds.size()) {
            throw new BusinessValidationException("one or more heroes do not exist");
        }

        if (status == ContentStatus.READY) {
            BigDecimal mainTotal = probabilities.stream()
                    .filter(probability -> probability.kind() == SummonProbabilityKind.MAIN)
                    .map(SummonProbabilityCreateRequest::valuePercent)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (mainTotal.compareTo(ONE_HUNDRED) != 0) {
                throw new BusinessValidationException("MAIN probabilities must total 100% for a READY summon");
            }
        }
    }

    private void validateBonusRules(
            List<SummonBonusRuleCreateRequest> bonusRules,
            List<SummonProbabilityCreateRequest> probabilities
    ) {
        Set<UUID> bonusDefinitionIds = bonusRules.stream()
                .map(SummonBonusRuleCreateRequest::definitionId)
                .collect(java.util.stream.Collectors.toSet());
        if (probabilityDefinitionRepository.findAllById(bonusDefinitionIds).size() != bonusDefinitionIds.size()) {
            throw new BusinessValidationException("one or more bonus definitions do not exist");
        }

        Set<UUID> configuredDefinitionIds = probabilities.stream()
                .map(SummonProbabilityCreateRequest::definitionId)
                .collect(java.util.stream.Collectors.toSet());

        for (SummonBonusRuleCreateRequest rule : bonusRules) {
            validateBonusRule(rule, configuredDefinitionIds);
        }
    }

    private void validateBonusRule(
            SummonBonusRuleCreateRequest rule,
            Set<UUID> configuredDefinitionIds
    ) {
        if (rule.kind() == SummonBonusKind.CONDITIONAL && rule.triggerCondition() == null) {
            throw new BusinessValidationException("CONDITIONAL bonus requires triggerCondition");
        }
        if (rule.kind() == SummonBonusKind.INDEPENDENT && rule.triggerCondition() != null) {
            throw new BusinessValidationException("INDEPENDENT bonus must not have triggerCondition");
        }
        if (rule.steps().size() > 1 && rule.continuationCondition() == null) {
            throw new BusinessValidationException("bonus with multiple steps requires continuationCondition");
        }

        validateCondition(rule.triggerCondition(), configuredDefinitionIds, "triggerCondition");
        validateCondition(rule.continuationCondition(), configuredDefinitionIds, "continuationCondition");

        Set<Integer> positions = new HashSet<>();
        for (SummonBonusStepCreateRequest step : rule.steps()) {
            if (!positions.add(step.position())) {
                throw new BusinessValidationException("bonus step positions must not contain duplicates");
            }

            if (step.rewardMode() == BonusRewardMode.MAIN_ROLL
                    && step.rewardProbabilityDefinitionId() != null) {
                throw new BusinessValidationException(
                        "MAIN_ROLL bonus step must not contain rewardProbabilityDefinitionId"
                );
            }

            if (step.rewardMode() == BonusRewardMode.SPECIFIC_PROBABILITY) {
                UUID definitionId = step.rewardProbabilityDefinitionId();
                if (definitionId == null) {
                    throw new BusinessValidationException(
                            "SPECIFIC_PROBABILITY bonus step requires rewardProbabilityDefinitionId"
                    );
                }
                if (!configuredDefinitionIds.contains(definitionId)) {
                    throw new BusinessValidationException(
                            "reward probability must be configured in the same summon variant"
                    );
                }
            }
        }
    }

    private void validateCondition(
            SummonBonusConditionRequest condition,
            Set<UUID> configuredDefinitionIds,
            String fieldName
    ) {
        if (condition == null) {
            return;
        }
        if (condition.type() == null) {
            throw new BusinessValidationException(fieldName + ".type must not be null");
        }

        if (condition.type() == BonusResultConditionType.ANY_RESULT) {
            if (condition.rarityId() != null || condition.probabilityDefinitionId() != null) {
                throw new BusinessValidationException(fieldName + " with ANY_RESULT must not contain a reference");
            }
            return;
        }

        if (condition.type() == BonusResultConditionType.RARITY) {
            if (condition.rarityId() == null || condition.probabilityDefinitionId() != null) {
                throw new BusinessValidationException(fieldName + " with RARITY requires only rarityId");
            }
            if (!rarityRepository.existsById(condition.rarityId())) {
                throw new BusinessValidationException("rarity does not exist: " + condition.rarityId());
            }
            return;
        }

        if (condition.probabilityDefinitionId() == null || condition.rarityId() != null) {
            throw new BusinessValidationException(
                    fieldName + " with PROBABILITY requires only probabilityDefinitionId"
            );
        }
        if (!configuredDefinitionIds.contains(condition.probabilityDefinitionId())) {
            throw new BusinessValidationException(
                    fieldName + " probability must be configured in the same summon variant"
            );
        }
    }

    private void validateLocalizedText(LocalizedTextJson value, String fieldName) {
        if (value == null || (!hasText(value.ru()) && !hasText(value.en()))) {
            throw new BusinessValidationException(fieldName + " must contain at least one localized value");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values.stream().filter(Objects::nonNull).toList();
    }
}
