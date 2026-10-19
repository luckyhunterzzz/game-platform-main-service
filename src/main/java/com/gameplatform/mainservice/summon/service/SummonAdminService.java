package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusRule;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStep;
import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonHeroId;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbabilityDefinition;
import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusConditionRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusRuleCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusStepCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonVariantCloneRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonAdminDetailsResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonAdminSummaryResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonBonusConditionResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonBonusRuleResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonBonusStepResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonProbabilityDefinitionResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonProbabilityResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonVariantResponse;
import com.gameplatform.mainservice.summon.repository.SummonBonusRuleRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepRepository;
import com.gameplatform.mainservice.summon.repository.SummonHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import com.gameplatform.mainservice.summon.repository.SummonVariantRepository;
import com.gameplatform.mainservice.summon.validation.SummonValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummonAdminService {

    private final SummonRepository summonRepository;
    private final SummonVariantRepository summonVariantRepository;
    private final SummonProbabilityDefinitionRepository probabilityDefinitionRepository;
    private final SummonProbabilityRepository probabilityRepository;
    private final SummonHeroRepository summonHeroRepository;
    private final SummonBonusRuleRepository bonusRuleRepository;
    private final SummonBonusStepRepository bonusStepRepository;
    private final SummonValidator summonValidator;
    private final CurrentUserProvider currentUserProvider;
    private final MediaUrlResolver mediaUrlResolver;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<SummonAdminSummaryResponse> getAll() {
        return summonRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toAdminSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SummonAdminDetailsResponse getById(UUID summonId) {
        Summon summon = summonRepository.findById(summonId)
                .orElseThrow(() -> new NotFoundException("Summon not found: " + summonId));
        List<SummonVariant> variants = summonVariantRepository.findAllBySummonId(summonId);

        SummonVariant defaultVariant = variants.stream()
                .filter(variant -> variant.getVariantType() == SummonVariantType.DEFAULT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Default summon variant not found: " + summonId
                ));
        List<SummonVariant> scheduledVariants = variants.stream()
                .filter(variant -> variant.getVariantType() == SummonVariantType.SCHEDULED)
                .sorted(Comparator.comparing(
                        SummonVariant::getStartsAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();

        return toAdminDetailsResponse(summon, defaultVariant, scheduledVariants);
    }

    @Transactional
    public SummonAdminDetailsResponse create(SummonCreateRequest request) {
        summonValidator.validateCreate(request);

        UUID actorId = currentUserProvider.getUserId();
        OffsetDateTime now = OffsetDateTime.now(clock);

        Summon summon = Summon.builder()
                .slug(normalizeSlug(request.slug()))
                .nameJson(normalizeRequiredText(request.nameJson()))
                .descriptionJson(normalizeOptionalText(request.descriptionJson()))
                .imageBucket(trimToNull(request.imageBucket()))
                .imageObjectKey(trimToNull(request.imageObjectKey()))
                .backgroundImageBucket(trimToNull(request.backgroundImageBucket()))
                .backgroundImageObjectKey(trimToNull(request.backgroundImageObjectKey()))
                .status(request.status())
                .createdBy(actorId)
                .updatedBy(actorId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Summon savedSummon = summonRepository.save(summon);
        SummonVariant defaultVariant = summonVariantRepository.save(SummonVariant.builder()
                .summonId(savedSummon.getId())
                .variantType(SummonVariantType.DEFAULT)
                .status(request.status())
                .createdBy(actorId)
                .updatedBy(actorId)
                .createdAt(now)
                .updatedAt(now)
                .build());

        Map<UUID, SummonProbability> probabilitiesByDefinition = createProbabilities(
                defaultVariant.getId(),
                safeList(request.probabilities())
        );
        createBonusRules(
                defaultVariant.getId(),
                safeList(request.bonusRules()),
                probabilitiesByDefinition
        );

        return toAdminDetailsResponse(savedSummon, defaultVariant, List.of());
    }

    @Transactional
    public SummonAdminDetailsResponse update(UUID summonId, SummonCreateRequest request) {
        Summon summon = summonRepository.findById(summonId)
                .orElseThrow(() -> new NotFoundException("Summon not found: " + summonId));
        summonValidator.validateUpdate(summonId, request);

        SummonVariant defaultVariant = summonVariantRepository.findAllBySummonId(summonId).stream()
                .filter(variant -> variant.getVariantType() == SummonVariantType.DEFAULT)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Default summon variant not found: " + summonId
                ));

        UUID actorId = currentUserProvider.getUserId();
        OffsetDateTime now = OffsetDateTime.now(clock);

        summon.setSlug(normalizeSlug(request.slug()));
        summon.setNameJson(normalizeRequiredText(request.nameJson()));
        summon.setDescriptionJson(normalizeOptionalText(request.descriptionJson()));
        summon.setImageBucket(trimToNull(request.imageBucket()));
        summon.setImageObjectKey(trimToNull(request.imageObjectKey()));
        summon.setBackgroundImageBucket(trimToNull(request.backgroundImageBucket()));
        summon.setBackgroundImageObjectKey(trimToNull(request.backgroundImageObjectKey()));
        summon.setStatus(request.status());
        summon.setUpdatedBy(actorId);
        summon.setUpdatedAt(now);

        defaultVariant.setStatus(request.status());
        defaultVariant.setUpdatedBy(actorId);
        defaultVariant.setUpdatedAt(now);

        replaceVariantConfiguration(defaultVariant.getId(), request);
        summonRepository.save(summon);
        summonVariantRepository.save(defaultVariant);

        List<SummonVariant> scheduledVariants = summonVariantRepository.findAllBySummonId(summonId).stream()
                .filter(variant -> variant.getVariantType() == SummonVariantType.SCHEDULED)
                .sorted(Comparator.comparing(
                        SummonVariant::getStartsAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();
        return toAdminDetailsResponse(summon, defaultVariant, scheduledVariants);
    }

    private void replaceVariantConfiguration(UUID variantId, SummonCreateRequest request) {
        List<SummonProbability> probabilities = probabilityRepository
                .findAllByVariantIdOrderByPositionAsc(variantId);
        List<SummonBonusRule> rules = bonusRuleRepository.findAllByVariantId(variantId);

        if (!rules.isEmpty()) {
            List<UUID> ruleIds = rules.stream().map(SummonBonusRule::getId).toList();
            List<SummonBonusStep> steps = bonusStepRepository
                    .findAllByBonusRuleIdInOrderByPositionAsc(ruleIds);
            if (!steps.isEmpty()) {
                bonusStepRepository.deleteAllInBatch(steps);
                bonusStepRepository.flush();
            }
            bonusRuleRepository.deleteAllInBatch(rules);
            bonusRuleRepository.flush();
        }

        if (!probabilities.isEmpty()) {
            List<SummonHero> heroLinks = summonHeroRepository.findAllByIdProbabilityIdIn(
                    probabilities.stream().map(SummonProbability::getId).toList()
            );
            if (!heroLinks.isEmpty()) {
                summonHeroRepository.deleteAllInBatch(heroLinks);
                summonHeroRepository.flush();
            }
            probabilityRepository.deleteAllInBatch(probabilities);
            probabilityRepository.flush();
        }

        Map<UUID, SummonProbability> probabilitiesByDefinition = createProbabilities(
                variantId,
                safeList(request.probabilities())
        );
        createBonusRules(
                variantId,
                safeList(request.bonusRules()),
                probabilitiesByDefinition
        );
    }

    @Transactional
    public SummonVariantResponse cloneVariant(
            UUID summonId,
            SummonVariantCloneRequest request
    ) {
        summonValidator.validateVariantClone(request);

        if (!summonRepository.existsById(summonId)) {
            throw new NotFoundException("Summon not found: " + summonId);
        }

        SummonVariant source = summonVariantRepository.findByIdAndSummonId(request.sourceVariantId(), summonId)
                .orElseThrow(() -> new NotFoundException(
                        "Summon variant not found: " + request.sourceVariantId()
                ));
        List<SummonProbability> sourceProbabilities = probabilityRepository
                .findAllByVariantIdOrderByPositionAsc(source.getId());
        summonValidator.validateVariantStatus(request.status(), sourceProbabilities);

        UUID actorId = currentUserProvider.getUserId();
        OffsetDateTime now = OffsetDateTime.now(clock);

        SummonVariant target = summonVariantRepository.save(SummonVariant.builder()
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .status(request.status())
                .createdBy(actorId)
                .updatedBy(actorId)
                .createdAt(now)
                .updatedAt(now)
                .build());

        cloneConfiguration(source.getId(), target.getId(), sourceProbabilities);
        return toVariantResponse(target);
    }

    private Map<UUID, SummonProbability> createProbabilities(
            UUID variantId,
            List<SummonProbabilityCreateRequest> requests
    ) {
        Map<UUID, SummonProbability> result = new HashMap<>();

        for (SummonProbabilityCreateRequest request : requests) {
            SummonProbability probability = probabilityRepository.save(SummonProbability.builder()
                    .variantId(variantId)
                    .definitionId(request.definitionId())
                    .kind(request.kind())
                    .valuePercent(request.valuePercent())
                    .position(request.position())
                    .build());

            result.put(request.definitionId(), probability);

            Set<Long> heroIds = new LinkedHashSet<>(safeList(request.heroIds()));
            if (!heroIds.isEmpty()) {
                summonHeroRepository.saveAll(heroIds.stream()
                        .map(heroId -> SummonHero.builder()
                                .id(new SummonHeroId(probability.getId(), heroId))
                                .build())
                        .toList());
            }
        }

        return result;
    }

    private void createBonusRules(
            UUID variantId,
            List<SummonBonusRuleCreateRequest> requests,
            Map<UUID, SummonProbability> probabilitiesByDefinition
    ) {
        for (SummonBonusRuleCreateRequest request : requests) {
            ResolvedCondition trigger = resolveCondition(request.triggerCondition(), probabilitiesByDefinition);
            ResolvedCondition continuation = resolveCondition(
                    request.continuationCondition(),
                    probabilitiesByDefinition
            );

            SummonBonusRule rule = bonusRuleRepository.save(SummonBonusRule.builder()
                    .variantId(variantId)
                    .definitionId(request.definitionId())
                    .kind(request.kind())
                    .userSelectable(request.userSelectable())
                    .triggerConditionType(trigger.type())
                    .triggerRarityId(trigger.rarityId())
                    .triggerProbabilityId(trigger.probabilityId())
                    .continuationConditionType(continuation.type())
                    .continuationRarityId(continuation.rarityId())
                    .continuationProbabilityId(continuation.probabilityId())
                    .build());

            List<SummonBonusStep> steps = request.steps().stream()
                    .map(step -> toBonusStep(rule.getId(), step, probabilitiesByDefinition))
                    .toList();
            bonusStepRepository.saveAll(steps);
        }
    }

    private SummonBonusStep toBonusStep(
            UUID ruleId,
            SummonBonusStepCreateRequest request,
            Map<UUID, SummonProbability> probabilitiesByDefinition
    ) {
        UUID rewardProbabilityId = null;
        if (request.rewardProbabilityDefinitionId() != null) {
            rewardProbabilityId = getConfiguredProbability(
                    request.rewardProbabilityDefinitionId(),
                    probabilitiesByDefinition
            ).getId();
        }

        return SummonBonusStep.builder()
                .bonusRuleId(ruleId)
                .position(request.position())
                .activationChancePercent(request.activationChancePercent())
                .rewardMode(request.rewardMode())
                .rewardProbabilityId(rewardProbabilityId)
                .build();
    }

    private ResolvedCondition resolveCondition(
            SummonBonusConditionRequest request,
            Map<UUID, SummonProbability> probabilitiesByDefinition
    ) {
        if (request == null) {
            return ResolvedCondition.empty();
        }

        UUID probabilityId = null;
        if (request.probabilityDefinitionId() != null) {
            probabilityId = getConfiguredProbability(
                    request.probabilityDefinitionId(),
                    probabilitiesByDefinition
            ).getId();
        }

        return new ResolvedCondition(request.type(), request.rarityId(), probabilityId);
    }

    private SummonProbability getConfiguredProbability(
            UUID definitionId,
            Map<UUID, SummonProbability> probabilitiesByDefinition
    ) {
        SummonProbability probability = probabilitiesByDefinition.get(definitionId);
        if (probability == null) {
            throw new BusinessValidationException(
                    "Probability definition is not configured in this summon variant: " + definitionId
            );
        }
        return probability;
    }

    private void cloneConfiguration(
            UUID sourceVariantId,
            UUID targetVariantId,
            List<SummonProbability> sourceProbabilities
    ) {
        Map<UUID, UUID> probabilityIdMap = new HashMap<>();

        for (SummonProbability source : sourceProbabilities) {
            SummonProbability target = probabilityRepository.save(SummonProbability.builder()
                    .variantId(targetVariantId)
                    .definitionId(source.getDefinitionId())
                    .kind(source.getKind())
                    .valuePercent(source.getValuePercent())
                    .position(source.getPosition())
                    .build());
            probabilityIdMap.put(source.getId(), target.getId());
        }

        cloneHeroes(sourceProbabilities, probabilityIdMap);
        cloneBonusRules(sourceVariantId, targetVariantId, probabilityIdMap);
    }

    private void cloneHeroes(
            List<SummonProbability> sourceProbabilities,
            Map<UUID, UUID> probabilityIdMap
    ) {
        List<UUID> sourceProbabilityIds = sourceProbabilities.stream()
                .map(SummonProbability::getId)
                .toList();
        if (sourceProbabilityIds.isEmpty()) {
            return;
        }

        List<SummonHero> clonedHeroes = summonHeroRepository.findAllByIdProbabilityIdIn(sourceProbabilityIds)
                .stream()
                .map(source -> SummonHero.builder()
                        .id(new SummonHeroId(
                                remapProbabilityId(source.getId().getProbabilityId(), probabilityIdMap),
                                source.getId().getHeroId()
                        ))
                        .build())
                .toList();
        summonHeroRepository.saveAll(clonedHeroes);
    }

    private void cloneBonusRules(
            UUID sourceVariantId,
            UUID targetVariantId,
            Map<UUID, UUID> probabilityIdMap
    ) {
        List<SummonBonusRule> sourceRules = bonusRuleRepository.findAllByVariantId(sourceVariantId);
        Map<UUID, UUID> ruleIdMap = new HashMap<>();

        for (SummonBonusRule source : sourceRules) {
            SummonBonusRule target = bonusRuleRepository.save(SummonBonusRule.builder()
                    .variantId(targetVariantId)
                    .definitionId(source.getDefinitionId())
                    .kind(source.getKind())
                    .userSelectable(source.isUserSelectable())
                    .triggerConditionType(source.getTriggerConditionType())
                    .triggerRarityId(source.getTriggerRarityId())
                    .triggerProbabilityId(remapNullableProbabilityId(
                            source.getTriggerProbabilityId(),
                            probabilityIdMap
                    ))
                    .continuationConditionType(source.getContinuationConditionType())
                    .continuationRarityId(source.getContinuationRarityId())
                    .continuationProbabilityId(remapNullableProbabilityId(
                            source.getContinuationProbabilityId(),
                            probabilityIdMap
                    ))
                    .build());
            ruleIdMap.put(source.getId(), target.getId());
        }

        if (sourceRules.isEmpty()) {
            return;
        }

        List<UUID> sourceRuleIds = sourceRules.stream().map(SummonBonusRule::getId).toList();
        List<SummonBonusStep> targetSteps = bonusStepRepository
                .findAllByBonusRuleIdInOrderByPositionAsc(sourceRuleIds)
                .stream()
                .map(source -> SummonBonusStep.builder()
                        .bonusRuleId(ruleIdMap.get(source.getBonusRuleId()))
                        .position(source.getPosition())
                        .activationChancePercent(source.getActivationChancePercent())
                        .rewardMode(source.getRewardMode())
                        .rewardProbabilityId(remapNullableProbabilityId(
                                source.getRewardProbabilityId(),
                                probabilityIdMap
                        ))
                        .build())
                .toList();
        bonusStepRepository.saveAll(targetSteps);
    }

    private UUID remapNullableProbabilityId(UUID sourceId, Map<UUID, UUID> probabilityIdMap) {
        return sourceId == null ? null : remapProbabilityId(sourceId, probabilityIdMap);
    }

    private UUID remapProbabilityId(UUID sourceId, Map<UUID, UUID> probabilityIdMap) {
        UUID targetId = probabilityIdMap.get(sourceId);
        if (targetId == null) {
            throw new BusinessValidationException(
                    "Source bonus references a probability outside its summon variant: " + sourceId
            );
        }
        return targetId;
    }

    private SummonAdminSummaryResponse toAdminSummaryResponse(Summon summon) {
        return new SummonAdminSummaryResponse(
                summon.getId(),
                summon.getSlug(),
                summon.getNameJson(),
                summon.getDescriptionJson(),
                summon.getStatus(),
                mediaUrlResolver.resolveUrl(summon.getImageBucket(), summon.getImageObjectKey()),
                mediaUrlResolver.resolveUrl(
                        summon.getBackgroundImageBucket(),
                        summon.getBackgroundImageObjectKey()
                ),
                summon.getUpdatedAt()
        );
    }

    private SummonAdminDetailsResponse toAdminDetailsResponse(
            Summon summon,
            SummonVariant defaultVariant,
            List<SummonVariant> scheduledVariants
    ) {
        return new SummonAdminDetailsResponse(
                summon.getId(),
                summon.getVersion(),
                summon.getSlug(),
                summon.getNameJson(),
                summon.getDescriptionJson(),
                summon.getStatus(),
                summon.getImageBucket(),
                summon.getImageObjectKey(),
                mediaUrlResolver.resolveUrl(summon.getImageBucket(), summon.getImageObjectKey()),
                summon.getBackgroundImageBucket(),
                summon.getBackgroundImageObjectKey(),
                mediaUrlResolver.resolveUrl(
                        summon.getBackgroundImageBucket(),
                        summon.getBackgroundImageObjectKey()
                ),
                summon.getCreatedBy(),
                summon.getUpdatedBy(),
                summon.getCreatedAt(),
                summon.getUpdatedAt(),
                toVariantResponse(defaultVariant),
                scheduledVariants.stream()
                        .map(this::toVariantResponse)
                        .toList()
        );
    }

    private SummonVariantResponse toVariantResponse(SummonVariant variant) {
        List<SummonProbability> probabilities = probabilityRepository
                .findAllByVariantIdOrderByPositionAsc(variant.getId());
        List<SummonBonusRule> rules = bonusRuleRepository.findAllByVariantId(variant.getId()).stream()
                .sorted(Comparator.comparing(rule -> rule.getKind().name()))
                .toList();
        Map<UUID, SummonProbabilityDefinition> definitionsById = probabilityDefinitionRepository
                .findAllById(loadDefinitionIds(probabilities, rules))
                .stream()
                .collect(Collectors.toMap(SummonProbabilityDefinition::getId, Function.identity()));

        Map<UUID, List<Long>> heroIdsByProbability = loadHeroIds(probabilities);
        Map<UUID, List<SummonBonusStep>> stepsByRule = loadSteps(rules);

        List<SummonProbabilityResponse> probabilityResponses = probabilities.stream()
                .map(probability -> toProbabilityResponse(
                        probability,
                        definitionsById.get(probability.getDefinitionId()),
                        heroIdsByProbability.getOrDefault(probability.getId(), List.of())
                ))
                .toList();

        List<SummonBonusRuleResponse> ruleResponses = rules.stream()
                .map(rule -> toBonusRuleResponse(
                        rule,
                        definitionsById.get(rule.getDefinitionId()),
                        stepsByRule.getOrDefault(rule.getId(), List.of())
                ))
                .toList();

        return new SummonVariantResponse(
                variant.getId(),
                variant.getVersion(),
                variant.getVariantType(),
                variant.getStartsAt(),
                variant.getEndsAt(),
                variant.getStatus(),
                variant.getCreatedBy(),
                variant.getUpdatedBy(),
                variant.getCreatedAt(),
                variant.getUpdatedAt(),
                probabilityResponses,
                ruleResponses
        );
    }

    private Map<UUID, List<Long>> loadHeroIds(List<SummonProbability> probabilities) {
        List<UUID> probabilityIds = probabilities.stream().map(SummonProbability::getId).toList();
        if (probabilityIds.isEmpty()) {
            return Map.of();
        }

        return summonHeroRepository.findAllByIdProbabilityIdIn(probabilityIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getId().getProbabilityId(),
                        Collectors.mapping(link -> link.getId().getHeroId(), Collectors.toList())
                ));
    }

    private Map<UUID, List<SummonBonusStep>> loadSteps(List<SummonBonusRule> rules) {
        List<UUID> ruleIds = rules.stream().map(SummonBonusRule::getId).toList();
        if (ruleIds.isEmpty()) {
            return Map.of();
        }

        return bonusStepRepository.findAllByBonusRuleIdInOrderByPositionAsc(ruleIds).stream()
                .collect(Collectors.groupingBy(SummonBonusStep::getBonusRuleId));
    }

    private SummonProbabilityResponse toProbabilityResponse(
            SummonProbability probability,
            SummonProbabilityDefinition definition,
            List<Long> heroIds
    ) {
        if (definition == null) {
            throw new IllegalStateException("Probability definition not found: " + probability.getDefinitionId());
        }

        List<Long> sortedHeroIds = heroIds.stream().sorted().toList();
        return new SummonProbabilityResponse(
                probability.getId(),
                new SummonProbabilityDefinitionResponse(
                        definition.getId(),
                        definition.getNameJson(),
                        definition.getDescriptionJson()
                ),
                probability.getKind(),
                probability.getValuePercent(),
                probability.getPosition(),
                sortedHeroIds
        );
    }

    private SummonBonusRuleResponse toBonusRuleResponse(
            SummonBonusRule rule,
            SummonProbabilityDefinition definition,
            List<SummonBonusStep> steps
    ) {
        if (definition == null) {
            throw new IllegalStateException("Bonus definition not found: " + rule.getDefinitionId());
        }

        return new SummonBonusRuleResponse(
                rule.getId(),
                new SummonProbabilityDefinitionResponse(
                        definition.getId(),
                        definition.getNameJson(),
                        definition.getDescriptionJson()
                ),
                rule.getKind(),
                rule.isUserSelectable(),
                toConditionResponse(
                        rule.getTriggerConditionType(),
                        rule.getTriggerRarityId(),
                        rule.getTriggerProbabilityId()
                ),
                toConditionResponse(
                        rule.getContinuationConditionType(),
                        rule.getContinuationRarityId(),
                        rule.getContinuationProbabilityId()
                ),
                steps.stream()
                        .sorted(Comparator.comparing(SummonBonusStep::getPosition))
                        .map(step -> new SummonBonusStepResponse(
                                step.getId(),
                                step.getPosition(),
                                step.getActivationChancePercent(),
                                step.getRewardMode(),
                                step.getRewardProbabilityId()
                        ))
                        .toList()
        );
    }

    private List<UUID> loadDefinitionIds(
            List<SummonProbability> probabilities,
            List<SummonBonusRule> rules
    ) {
        Set<UUID> definitionIds = new LinkedHashSet<>();
        probabilities.stream().map(SummonProbability::getDefinitionId).forEach(definitionIds::add);
        rules.stream().map(SummonBonusRule::getDefinitionId).forEach(definitionIds::add);
        return List.copyOf(definitionIds);
    }

    private SummonBonusConditionResponse toConditionResponse(
            BonusResultConditionType type,
            Long rarityId,
            UUID probabilityId
    ) {
        if (type == null) {
            return null;
        }
        return new SummonBonusConditionResponse(type, rarityId, probabilityId);
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }

    private LocalizedTextJson normalizeRequiredText(LocalizedTextJson value) {
        LocalizedTextJson normalized = normalizeOptionalText(value);
        if (normalized == null) {
            throw new BusinessValidationException("nameJson must contain at least one localized value");
        }
        return normalized;
    }

    private LocalizedTextJson normalizeOptionalText(LocalizedTextJson value) {
        if (value == null) {
            return null;
        }
        String ru = trimToNull(value.ru());
        String en = trimToNull(value.en());
        return ru == null && en == null ? null : new LocalizedTextJson(ru, en);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private record ResolvedCondition(
            BonusResultConditionType type,
            Long rarityId,
            UUID probabilityId
    ) {
        private static ResolvedCondition empty() {
            return new ResolvedCondition(null, null, null);
        }
    }
}
