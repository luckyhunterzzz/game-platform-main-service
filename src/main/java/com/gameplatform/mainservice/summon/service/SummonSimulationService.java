package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusRule;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStep;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbabilityDefinition;
import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonSimulationResultSource;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusStepHeroSelectionRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityHeroSelectionRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonSimulationRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonProbabilityDefinitionResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationBonusRuleResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationBonusStepResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationConfigResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationHeroResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationProbabilityResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationResultItemResponse;
import com.gameplatform.mainservice.summon.repository.SummonBonusRuleRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepRepository;
import com.gameplatform.mainservice.summon.repository.SummonHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import com.gameplatform.mainservice.summon.repository.SummonVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummonSimulationService {

    private static final int PERCENT_SCALE = 100;
    private static final int ONE_HUNDRED_PERCENT = 10_000;
    private static final int MAX_RESULTS = 1_000;
    private static final Set<Integer> ALLOWED_PULL_COUNTS = Set.of(1, 10, 30);

    private final SummonRepository summonRepository;
    private final SummonVariantRepository variantRepository;
    private final SummonProbabilityRepository probabilityRepository;
    private final SummonProbabilityDefinitionRepository definitionRepository;
    private final SummonHeroRepository summonHeroRepository;
    private final SummonBonusRuleRepository bonusRuleRepository;
    private final SummonBonusStepRepository bonusStepRepository;
    private final SummonBonusStepProbabilityRepository bonusStepProbabilityRepository;
    private final SummonBonusStepHeroRepository bonusStepHeroRepository;
    private final HeroRepository heroRepository;
    private final RarityRepository rarityRepository;
    private final MediaUrlResolver mediaUrlResolver;
    private final SummonRandomSource randomSource;

    @Transactional(readOnly = true)
    public SummonSimulationConfigResponse getConfig(UUID summonId, UUID variantId) {
        SimulationContext context = loadContext(summonId, variantId);
        List<Hero> selectableHeroes = heroRepository.findAll().stream()
                .toList();
        Map<Long, Rarity> selectableRaritiesById = loadRarities(selectableHeroes);
        selectableHeroes = selectableHeroes.stream()
                .filter(hero -> isFiveStar(hero, selectableRaritiesById))
                .sorted(Comparator.comparing(Hero::getSlug, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Set<Long> configuredHeroIds = new LinkedHashSet<>();
        context.heroIdsByProbability().values().forEach(configuredHeroIds::addAll);
        context.heroIdsByStep().values().forEach(configuredHeroIds::addAll);
        configuredHeroIds.addAll(selectableHeroes.stream().map(Hero::getId).toList());
        Map<Long, Hero> heroesById = heroRepository.findAllById(configuredHeroIds).stream()
                .collect(Collectors.toMap(Hero::getId, Function.identity()));
        Map<Long, Rarity> raritiesById = loadRarities(heroesById.values());

        List<SummonSimulationProbabilityResponse> probabilities = context.probabilities().stream()
                .filter(probability -> probability.getKind() == SummonProbabilityKind.MAIN)
                .map(probability -> new SummonSimulationProbabilityResponse(
                        probability.getId(),
                        definitionResponse(context, probability.getDefinitionId()),
                        probability.getValuePercent(),
                        probability.getPosition(),
                        probability.isUserSelectionEnabled(),
                        probability.getDefaultSelectedHeroCount(),
                        context.heroIdsByProbability().getOrDefault(probability.getId(), List.of()).stream()
                                .map(heroesById::get)
                                .filter(Objects::nonNull)
                                .map(hero -> toHeroResponse(hero, raritiesById))
                                .toList()
                ))
                .toList();

        List<SummonSimulationBonusRuleResponse> bonusRules = context.rules().stream()
                .map(rule -> new SummonSimulationBonusRuleResponse(
                        rule.getId(),
                        definitionResponse(context, rule.getDefinitionId()),
                        rule.getKind(),
                        rule.isUserSelectable(),
                        context.stepsByRule().getOrDefault(rule.getId(), List.of()).stream()
                                .map(step -> new SummonSimulationBonusStepResponse(
                                        step.getId(),
                                        step.getPosition(),
                                        step.getActivationChancePercent(),
                                        step.getRewardMode(),
                                        step.isUserHeroSelectionEnabled(),
                                        fixedHeroResponse(step, context, heroesById, raritiesById)
                                ))
                                .toList()
                ))
                .toList();

        Summon summon = context.summon();
        SummonVariant variant = context.variant();
        Map<Long, Rarity> finalRaritiesById = raritiesById;
        return new SummonSimulationConfigResponse(
                summon.getId(),
                summon.getSlug(),
                summon.getNameJson(),
                summon.getDescriptionJson(),
                mediaUrlResolver.resolveUrl(summon.getImageBucket(), summon.getImageObjectKey()),
                mediaUrlResolver.resolveUrl(summon.getBackgroundImageBucket(), summon.getBackgroundImageObjectKey()),
                variant.getId(),
                variant.getVersion(),
                variant.getVariantType(),
                variant.getStartsAt(),
                variant.getEndsAt(),
                probabilities,
                bonusRules,
                selectableHeroes.stream().map(hero -> toHeroResponse(hero, finalRaritiesById)).toList()
        );
    }

    @Transactional(readOnly = true)
    public SummonSimulationResponse simulate(
            UUID summonId,
            UUID variantId,
            SummonSimulationRequest request
    ) {
        SimulationContext context = loadContext(summonId, variantId);
        if (!Objects.equals(context.variant().getVersion(), request.variantVersion())) {
            throw new BusinessValidationException("Summon configuration has changed. Reload it and try again");
        }
        if (!ALLOWED_PULL_COUNTS.contains(request.pullCount())) {
            throw new BusinessValidationException("pullCount must be 1, 10, or 30");
        }

        Map<UUID, List<Long>> probabilitySelections = probabilitySelections(request.probabilityHeroSelections());
        Map<UUID, Long> stepSelections = stepSelections(request.bonusStepHeroSelections());
        Set<UUID> enabledSelectableRuleIds = enabledRuleIds(request.enabledBonusRuleIds(), context);
        Set<UUID> activeRuleIds = context.rules().stream()
                .filter(rule -> !rule.isUserSelectable() || enabledSelectableRuleIds.contains(rule.getId()))
                .map(SummonBonusRule::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Map<UUID, List<Long>> effectivePools = effectiveProbabilityPools(context, probabilitySelections);
        validateStepSelections(context, activeRuleIds, stepSelections);

        Set<Long> usedHeroIds = effectivePools.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        usedHeroIds.addAll(context.heroIdsByStep().values().stream().flatMap(List::stream).toList());
        usedHeroIds.addAll(stepSelections.values());
        Map<Long, Hero> heroesById = heroRepository.findAllById(usedHeroIds).stream()
                .collect(Collectors.toMap(Hero::getId, Function.identity()));
        if (heroesById.size() != usedHeroIds.size()) {
            throw new BusinessValidationException("One or more selected summon heroes do not exist");
        }
        Map<Long, Rarity> raritiesById = loadRarities(heroesById.values());
        validateUserSelectedHeroes(probabilitySelections.values(), stepSelections.values(), heroesById, raritiesById);
        validateReachablePools(context, effectivePools, heroesById);
        validateFixedBonusHeroes(context, activeRuleIds, stepSelections, heroesById);

        RuntimeState state = new RuntimeState(
                context,
                effectivePools,
                stepSelections,
                heroesById,
                raritiesById,
                new ArrayList<>()
        );
        List<SummonProbability> mainProbabilities = context.probabilities().stream()
                .filter(probability -> probability.getKind() == SummonProbabilityKind.MAIN)
                .toList();
        validateDistribution(
                mainProbabilities.stream()
                        .map(probability -> new WeightedProbability(
                                probability.getId(),
                                toHundredths(probability.getValuePercent())
                        ))
                        .toList(),
                "MAIN probabilities must total 100%"
        );

        List<SummonBonusRule> conditionalRules = context.rules().stream()
                .filter(rule -> activeRuleIds.contains(rule.getId()))
                .filter(rule -> rule.getKind() == SummonBonusKind.CONDITIONAL)
                .toList();
        List<SummonBonusRule> independentRules = context.rules().stream()
                .filter(rule -> activeRuleIds.contains(rule.getId()))
                .filter(rule -> rule.getKind() == SummonBonusKind.INDEPENDENT)
                .toList();

        for (int rootPullNumber = 1; rootPullNumber <= request.pullCount(); rootPullNumber++) {
            GeneratedResult baseResult = rollProbabilityDistribution(
                    mainProbabilities.stream()
                            .map(probability -> new WeightedProbability(
                                    probability.getId(),
                                    toHundredths(probability.getValuePercent())
                            ))
                            .toList(),
                    rootPullNumber,
                    null,
                    SummonSimulationResultSource.BASE,
                    null,
                    null,
                    state
            );

            List<GeneratedResult> conditionalResults = new ArrayList<>();
            for (SummonBonusRule rule : conditionalRules) {
                if (conditionMatches(
                        rule.getTriggerConditionType(),
                        rule.getTriggerRarityId(),
                        rule.getTriggerProbabilityId(),
                        baseResult
                )) {
                    conditionalResults.addAll(runRuleChain(rule, baseResult, state));
                }
            }

            List<GeneratedResult> independentSources = new ArrayList<>();
            independentSources.add(baseResult);
            independentSources.addAll(conditionalResults);
            for (GeneratedResult source : independentSources) {
                for (SummonBonusRule rule : independentRules) {
                    runRuleChain(rule, source, state);
                }
            }
        }

        List<SummonSimulationResultItemResponse> results = state.results().stream()
                .map(result -> new SummonSimulationResultItemResponse(
                        result.sequence(),
                        result.rootPullNumber(),
                        result.parentSequence(),
                        result.source(),
                        result.probabilityId(),
                        result.bonusRuleId(),
                        result.bonusStepId(),
                        toHeroResponse(result.hero(), raritiesById)
                ))
                .toList();
        return new SummonSimulationResponse(
                summonId,
                variantId,
                context.variant().getVersion(),
                request.pullCount(),
                results.size(),
                results
        );
    }

    private List<GeneratedResult> runRuleChain(
            SummonBonusRule rule,
            GeneratedResult source,
            RuntimeState state
    ) {
        List<GeneratedResult> generated = new ArrayList<>();
        GeneratedResult previous = source;
        List<SummonBonusStep> steps = state.context().stepsByRule().getOrDefault(rule.getId(), List.of());
        for (int index = 0; index < steps.size(); index++) {
            if (index > 0 && !conditionMatches(
                    rule.getContinuationConditionType(),
                    rule.getContinuationRarityId(),
                    rule.getContinuationProbabilityId(),
                    previous
            )) {
                break;
            }
            SummonBonusStep step = steps.get(index);
            if (!activated(step.getActivationChancePercent())) {
                break;
            }
            previous = rollBonusStep(rule, step, previous, state);
            generated.add(previous);
        }
        return generated;
    }

    private GeneratedResult rollBonusStep(
            SummonBonusRule rule,
            SummonBonusStep step,
            GeneratedResult parent,
            RuntimeState state
    ) {
        SummonSimulationResultSource source = rule.getKind() == SummonBonusKind.CONDITIONAL
                ? SummonSimulationResultSource.CONDITIONAL_BONUS
                : SummonSimulationResultSource.INDEPENDENT_BONUS;
        if (step.getRewardMode() == BonusRewardMode.MAIN_ROLL) {
            List<WeightedProbability> distribution = state.context().probabilities().stream()
                    .filter(probability -> probability.getKind() == SummonProbabilityKind.MAIN)
                    .map(probability -> new WeightedProbability(
                            probability.getId(),
                            toHundredths(probability.getValuePercent())
                    ))
                    .toList();
            return rollProbabilityDistribution(
                    distribution,
                    parent.rootPullNumber(),
                    parent.sequence(),
                    source,
                    rule.getId(),
                    step.getId(),
                    state
            );
        }
        if (step.getRewardMode() == BonusRewardMode.CUSTOM_DISTRIBUTION) {
            List<WeightedProbability> distribution = state.context().rewardProbabilitiesByStep()
                    .getOrDefault(step.getId(), List.of()).stream()
                    .map(link -> new WeightedProbability(
                            link.getId().getProbabilityId(),
                            toHundredths(link.getValuePercent())
                    ))
                    .toList();
            validateDistribution(distribution, "Bonus reward probabilities must total 100%");
            return rollProbabilityDistribution(
                    distribution,
                    parent.rootPullNumber(),
                    parent.sequence(),
                    source,
                    rule.getId(),
                    step.getId(),
                    state
            );
        }

        Long heroId = step.isUserHeroSelectionEnabled()
                ? state.stepSelections().get(step.getId())
                : onlyConfiguredHeroId(step.getId(), state.context());
        Hero hero = state.heroesById().get(heroId);
        return appendResult(
                parent.rootPullNumber(),
                parent.sequence(),
                source,
                null,
                rule.getId(),
                step.getId(),
                hero,
                state
        );
    }

    private GeneratedResult rollProbabilityDistribution(
            List<WeightedProbability> distribution,
            int rootPullNumber,
            Integer parentSequence,
            SummonSimulationResultSource source,
            UUID bonusRuleId,
            UUID bonusStepId,
            RuntimeState state
    ) {
        UUID probabilityId = chooseWeighted(distribution);
        List<Long> heroIds = state.effectivePools().getOrDefault(probabilityId, List.of());
        if (heroIds.isEmpty()) {
            throw new BusinessValidationException("Summon probability has no available heroes: " + probabilityId);
        }
        Long heroId = heroIds.get(randomSource.nextInt(heroIds.size()));
        Hero hero = state.heroesById().get(heroId);
        return appendResult(
                rootPullNumber,
                parentSequence,
                source,
                probabilityId,
                bonusRuleId,
                bonusStepId,
                hero,
                state
        );
    }

    private GeneratedResult appendResult(
            int rootPullNumber,
            Integer parentSequence,
            SummonSimulationResultSource source,
            UUID probabilityId,
            UUID bonusRuleId,
            UUID bonusStepId,
            Hero hero,
            RuntimeState state
    ) {
        if (state.results().size() >= MAX_RESULTS) {
            throw new BusinessValidationException("Summon simulation result limit exceeded");
        }
        if (hero == null) {
            throw new BusinessValidationException("Summon result hero is unavailable");
        }
        GeneratedResult result = new GeneratedResult(
                state.results().size() + 1,
                rootPullNumber,
                parentSequence,
                source,
                probabilityId,
                bonusRuleId,
                bonusStepId,
                hero
        );
        state.results().add(result);
        return result;
    }

    private UUID chooseWeighted(List<WeightedProbability> distribution) {
        validateDistribution(distribution, "Probability distribution must total 100%");
        int value = randomSource.nextInt(ONE_HUNDRED_PERCENT);
        int cumulative = 0;
        for (WeightedProbability probability : distribution) {
            cumulative += probability.weight();
            if (value < cumulative) {
                return probability.probabilityId();
            }
        }
        throw new IllegalStateException("Unable to choose summon probability");
    }

    private boolean activated(BigDecimal chancePercent) {
        int chance = toHundredths(chancePercent);
        if (chance <= 0) {
            return false;
        }
        if (chance >= ONE_HUNDRED_PERCENT) {
            return true;
        }
        return randomSource.nextInt(ONE_HUNDRED_PERCENT) < chance;
    }

    private boolean conditionMatches(
            BonusResultConditionType type,
            Long rarityId,
            UUID probabilityId,
            GeneratedResult result
    ) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case ANY_RESULT -> true;
            case RARITY -> Objects.equals(result.hero().getRarityId(), rarityId);
            case PROBABILITY -> Objects.equals(result.probabilityId(), probabilityId);
        };
    }

    private Map<UUID, List<Long>> probabilitySelections(
            List<SummonProbabilityHeroSelectionRequest> selections
    ) {
        Map<UUID, List<Long>> result = new LinkedHashMap<>();
        for (SummonProbabilityHeroSelectionRequest selection : selections) {
            if (result.putIfAbsent(selection.probabilityId(), selection.heroIds()) != null) {
                throw new BusinessValidationException("Probability hero selections must not contain duplicates");
            }
            if (new HashSet<>(selection.heroIds()).size() != selection.heroIds().size()) {
                throw new BusinessValidationException("Selected probability heroes must not contain duplicates");
            }
        }
        return result;
    }

    private Map<UUID, Long> stepSelections(List<SummonBonusStepHeroSelectionRequest> selections) {
        Map<UUID, Long> result = new LinkedHashMap<>();
        for (SummonBonusStepHeroSelectionRequest selection : selections) {
            if (result.putIfAbsent(selection.bonusStepId(), selection.heroId()) != null) {
                throw new BusinessValidationException("Bonus step hero selections must not contain duplicates");
            }
        }
        return result;
    }

    private Set<UUID> enabledRuleIds(List<UUID> enabledIds, SimulationContext context) {
        Set<UUID> result = new LinkedHashSet<>(enabledIds);
        if (result.size() != enabledIds.size()) {
            throw new BusinessValidationException("Enabled bonus rules must not contain duplicates");
        }
        Map<UUID, SummonBonusRule> rulesById = context.rules().stream()
                .collect(Collectors.toMap(SummonBonusRule::getId, Function.identity()));
        for (UUID ruleId : result) {
            SummonBonusRule rule = rulesById.get(ruleId);
            if (rule == null || !rule.isUserSelectable()) {
                throw new BusinessValidationException("Enabled bonus rule is not user-selectable: " + ruleId);
            }
        }
        return result;
    }

    private Map<UUID, List<Long>> effectiveProbabilityPools(
            SimulationContext context,
            Map<UUID, List<Long>> selections
    ) {
        Set<UUID> selectableProbabilityIds = context.probabilities().stream()
                .filter(SummonProbability::isUserSelectionEnabled)
                .map(SummonProbability::getId)
                .collect(Collectors.toSet());
        if (!selectableProbabilityIds.equals(selections.keySet())) {
            throw new BusinessValidationException(
                    "Hero selections must be provided for every user-selectable probability and no others"
            );
        }

        Map<UUID, List<Long>> result = new HashMap<>();
        for (SummonProbability probability : context.probabilities()) {
            result.put(
                    probability.getId(),
                    probability.isUserSelectionEnabled()
                            ? List.copyOf(selections.get(probability.getId()))
                            : context.heroIdsByProbability().getOrDefault(probability.getId(), List.of())
            );
        }
        return result;
    }

    private void validateStepSelections(
            SimulationContext context,
            Set<UUID> activeRuleIds,
            Map<UUID, Long> selections
    ) {
        Set<UUID> requiredStepIds = context.rules().stream()
                .filter(rule -> activeRuleIds.contains(rule.getId()))
                .flatMap(rule -> context.stepsByRule().getOrDefault(rule.getId(), List.of()).stream())
                .filter(SummonBonusStep::isUserHeroSelectionEnabled)
                .map(SummonBonusStep::getId)
                .collect(Collectors.toSet());
        if (!requiredStepIds.equals(selections.keySet())) {
            throw new BusinessValidationException(
                    "Hero selections must be provided for every active user-selectable bonus step and no others"
            );
        }
    }

    private void validateUserSelectedHeroes(
            Iterable<List<Long>> probabilitySelections,
            Iterable<Long> stepSelections,
            Map<Long, Hero> heroesById,
            Map<Long, Rarity> raritiesById
    ) {
        Set<Long> selectedIds = new LinkedHashSet<>();
        probabilitySelections.forEach(selectedIds::addAll);
        stepSelections.forEach(selectedIds::add);
        for (Long heroId : selectedIds) {
            Hero hero = heroesById.get(heroId);
            if (hero == null || !isFiveStar(hero, raritiesById)) {
                throw new BusinessValidationException("User-selected summon heroes must be 5-star heroes");
            }
        }
    }

    private void validateReachablePools(
            SimulationContext context,
            Map<UUID, List<Long>> effectivePools,
            Map<Long, Hero> heroesById
    ) {
        Set<UUID> reachableProbabilityIds = context.probabilities().stream()
                .filter(probability -> probability.getKind() == SummonProbabilityKind.MAIN)
                .filter(probability -> toHundredths(probability.getValuePercent()) > 0)
                .map(SummonProbability::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        context.rewardProbabilitiesByStep().values().stream()
                .flatMap(List::stream)
                .filter(link -> toHundredths(link.getValuePercent()) > 0)
                .map(link -> link.getId().getProbabilityId())
                .forEach(reachableProbabilityIds::add);

        for (UUID probabilityId : reachableProbabilityIds) {
            List<Long> heroIds = effectivePools.getOrDefault(probabilityId, List.of());
            if (heroIds.isEmpty()) {
                throw new BusinessValidationException("Summon probability has no heroes: " + probabilityId);
            }
            boolean containsUnavailableHero = heroIds.stream()
                    .map(heroesById::get)
                    .anyMatch(Objects::isNull);
            if (containsUnavailableHero) {
                throw new BusinessValidationException("Summon probability contains a missing hero: " + probabilityId);
            }
        }
    }

    private void validateFixedBonusHeroes(
            SimulationContext context,
            Set<UUID> activeRuleIds,
            Map<UUID, Long> stepSelections,
            Map<Long, Hero> heroesById
    ) {
        for (SummonBonusRule rule : context.rules()) {
            if (!activeRuleIds.contains(rule.getId())) {
                continue;
            }
            for (SummonBonusStep step : context.stepsByRule().getOrDefault(rule.getId(), List.of())) {
                if (step.getRewardMode() != BonusRewardMode.SPECIFIC_HERO_POOL) {
                    continue;
                }
                Long heroId = step.isUserHeroSelectionEnabled()
                        ? stepSelections.get(step.getId())
                        : onlyConfiguredHeroId(step.getId(), context);
                Hero hero = heroesById.get(heroId);
                if (hero == null) {
                    throw new BusinessValidationException("Specific bonus hero is missing: " + step.getId());
                }
            }
        }
    }

    private Long onlyConfiguredHeroId(UUID stepId, SimulationContext context) {
        List<Long> heroIds = context.heroIdsByStep().getOrDefault(stepId, List.of());
        if (heroIds.size() != 1) {
            throw new BusinessValidationException("Specific bonus step must contain exactly one hero: " + stepId);
        }
        return heroIds.get(0);
    }

    private void validateDistribution(List<WeightedProbability> distribution, String message) {
        int total = distribution.stream().mapToInt(WeightedProbability::weight).sum();
        if (distribution.isEmpty() || total != ONE_HUNDRED_PERCENT) {
            throw new BusinessValidationException(message);
        }
    }

    private int toHundredths(BigDecimal value) {
        try {
            return value.multiply(BigDecimal.valueOf(PERCENT_SCALE)).intValueExact();
        } catch (ArithmeticException exception) {
            throw new BusinessValidationException("Summon probability must have at most two decimal places");
        }
    }

    private SimulationContext loadContext(UUID summonId, UUID variantId) {
        Summon summon = summonRepository.findById(summonId)
                .orElseThrow(() -> new NotFoundException("Summon not found: " + summonId));
        SummonVariant variant = variantRepository.findByIdAndSummonId(variantId, summonId)
                .orElseThrow(() -> new NotFoundException("Summon variant not found: " + variantId));
        if (variant.getStatus() != ContentStatus.READY
                || (variant.getVariantType() == SummonVariantType.DEFAULT
                && summon.getStatus() != ContentStatus.READY)) {
            throw new NotFoundException("Public summon variant not found: " + variantId);
        }

        List<SummonProbability> probabilities = probabilityRepository
                .findAllByVariantIdOrderByPositionAsc(variantId);
        List<UUID> probabilityIds = probabilities.stream().map(SummonProbability::getId).toList();
        Map<UUID, List<Long>> heroIdsByProbability = probabilityIds.isEmpty()
                ? Map.of()
                : summonHeroRepository.findAllByIdProbabilityIdIn(probabilityIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getId().getProbabilityId(),
                        Collectors.mapping(
                                link -> link.getId().getHeroId(),
                                Collectors.collectingAndThen(Collectors.toList(), values -> values.stream().sorted().toList())
                        )
                ));

        List<SummonBonusRule> rules = bonusRuleRepository.findAllByVariantId(variantId).stream()
                .sorted(Comparator.comparing(rule -> rule.getId().toString()))
                .toList();
        List<UUID> ruleIds = rules.stream().map(SummonBonusRule::getId).toList();
        List<SummonBonusStep> steps = ruleIds.isEmpty()
                ? List.of()
                : bonusStepRepository.findAllByBonusRuleIdInOrderByPositionAsc(ruleIds);
        Map<UUID, List<SummonBonusStep>> stepsByRule = steps.stream()
                .collect(Collectors.groupingBy(
                        SummonBonusStep::getBonusRuleId,
                        Collectors.collectingAndThen(Collectors.toList(), values -> values.stream()
                                .sorted(Comparator.comparing(SummonBonusStep::getPosition))
                                .toList())
                ));
        List<UUID> stepIds = steps.stream().map(SummonBonusStep::getId).toList();
        Map<UUID, List<SummonBonusStepProbability>> rewardProbabilitiesByStep = stepIds.isEmpty()
                ? Map.of()
                : bonusStepProbabilityRepository.findAllByIdBonusStepIdIn(stepIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getId().getBonusStepId(),
                        Collectors.collectingAndThen(Collectors.toList(), values -> values.stream()
                                .sorted(Comparator.comparing(SummonBonusStepProbability::getPosition))
                                .toList())
                ));
        Map<UUID, List<Long>> heroIdsByStep = stepIds.isEmpty()
                ? Map.of()
                : bonusStepHeroRepository.findAllByIdBonusStepIdIn(stepIds).stream()
                .collect(Collectors.groupingBy(
                        link -> link.getId().getBonusStepId(),
                        Collectors.mapping(
                                link -> link.getId().getHeroId(),
                                Collectors.collectingAndThen(Collectors.toList(), values -> values.stream().sorted().toList())
                        )
                ));

        Set<UUID> definitionIds = probabilities.stream()
                .map(SummonProbability::getDefinitionId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        rules.stream().map(SummonBonusRule::getDefinitionId).forEach(definitionIds::add);
        Map<UUID, SummonProbabilityDefinition> definitionsById = definitionRepository.findAllById(definitionIds)
                .stream()
                .collect(Collectors.toMap(SummonProbabilityDefinition::getId, Function.identity()));

        return new SimulationContext(
                summon,
                variant,
                probabilities,
                probabilities.stream().collect(Collectors.toMap(SummonProbability::getId, Function.identity())),
                heroIdsByProbability,
                rules,
                stepsByRule,
                rewardProbabilitiesByStep,
                heroIdsByStep,
                definitionsById
        );
    }

    private SummonProbabilityDefinitionResponse definitionResponse(
            SimulationContext context,
            UUID definitionId
    ) {
        SummonProbabilityDefinition definition = context.definitionsById().get(definitionId);
        if (definition == null) {
            throw new IllegalStateException("Summon probability definition not found: " + definitionId);
        }
        return new SummonProbabilityDefinitionResponse(
                definition.getId(),
                definition.getNameJson(),
                definition.getDescriptionJson()
        );
    }

    private SummonSimulationHeroResponse fixedHeroResponse(
            SummonBonusStep step,
            SimulationContext context,
            Map<Long, Hero> heroesById,
            Map<Long, Rarity> raritiesById
    ) {
        if (step.getRewardMode() != BonusRewardMode.SPECIFIC_HERO_POOL
                || step.isUserHeroSelectionEnabled()) {
            return null;
        }
        List<Long> heroIds = context.heroIdsByStep().getOrDefault(step.getId(), List.of());
        if (heroIds.size() != 1) {
            return null;
        }
        Hero hero = heroesById.get(heroIds.get(0));
        return hero == null ? null : toHeroResponse(hero, raritiesById);
    }

    private SummonSimulationHeroResponse toHeroResponse(Hero hero, Map<Long, Rarity> raritiesById) {
        Rarity rarity = raritiesById.get(hero.getRarityId());
        return new SummonSimulationHeroResponse(
                hero.getId(),
                hero.getSlug(),
                hero.getNameJson(),
                mediaUrlResolver.resolveUrl(hero.getPreviewBucket(), hero.getPreviewObjectKey()),
                heroImageUrls(hero),
                rarity == null ? null : rarity.getStars()
        );
    }

    private LocalizedTextJson heroImageUrls(Hero hero) {
        String ruBucket = localizedValue(hero.getImageBucketJson(), true);
        String ruObjectKey = localizedValue(hero.getImageObjectKeyJson(), true);
        String enBucket = localizedValue(hero.getImageBucketJson(), false);
        String enObjectKey = localizedValue(hero.getImageObjectKeyJson(), false);
        return new LocalizedTextJson(
                mediaUrlResolver.resolveUrl(ruBucket, ruObjectKey),
                mediaUrlResolver.resolveUrl(enBucket, enObjectKey)
        );
    }

    private String localizedValue(LocalizedTextJson value, boolean russian) {
        if (value == null) {
            return null;
        }
        String primary = russian ? value.ru() : value.en();
        String secondary = russian ? value.en() : value.ru();
        return primary != null && !primary.isBlank() ? primary : secondary;
    }

    private Map<Long, Rarity> loadRarities(Iterable<Hero> heroes) {
        Set<Long> rarityIds = new LinkedHashSet<>();
        heroes.forEach(hero -> rarityIds.add(hero.getRarityId()));
        return rarityRepository.findAllById(rarityIds).stream()
                .collect(Collectors.toMap(Rarity::getId, Function.identity()));
    }

    private boolean isFiveStar(Hero hero, Map<Long, Rarity> raritiesById) {
        Rarity rarity = raritiesById.get(hero.getRarityId());
        return rarity != null && Objects.equals(rarity.getStars(), 5);
    }

    private record SimulationContext(
            Summon summon,
            SummonVariant variant,
            List<SummonProbability> probabilities,
            Map<UUID, SummonProbability> probabilitiesById,
            Map<UUID, List<Long>> heroIdsByProbability,
            List<SummonBonusRule> rules,
            Map<UUID, List<SummonBonusStep>> stepsByRule,
            Map<UUID, List<SummonBonusStepProbability>> rewardProbabilitiesByStep,
            Map<UUID, List<Long>> heroIdsByStep,
            Map<UUID, SummonProbabilityDefinition> definitionsById
    ) {
    }

    private record RuntimeState(
            SimulationContext context,
            Map<UUID, List<Long>> effectivePools,
            Map<UUID, Long> stepSelections,
            Map<Long, Hero> heroesById,
            Map<Long, Rarity> raritiesById,
            List<GeneratedResult> results
    ) {
    }

    private record WeightedProbability(UUID probabilityId, int weight) {
    }

    private record GeneratedResult(
            int sequence,
            int rootPullNumber,
            Integer parentSequence,
            SummonSimulationResultSource source,
            UUID probabilityId,
            UUID bonusRuleId,
            UUID bonusStepId,
            Hero hero
    ) {
    }
}
