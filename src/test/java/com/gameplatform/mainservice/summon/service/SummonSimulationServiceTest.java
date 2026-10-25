package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusRule;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStep;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepHeroId;
import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonHeroId;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonSimulationResultSource;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import com.gameplatform.mainservice.summon.dto.request.SummonSimulationRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonSimulationResponse;
import com.gameplatform.mainservice.summon.repository.SummonBonusRuleRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepRepository;
import com.gameplatform.mainservice.summon.repository.SummonHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import com.gameplatform.mainservice.summon.repository.SummonVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummonSimulationServiceTest {

    @Mock private SummonRepository summonRepository;
    @Mock private SummonVariantRepository variantRepository;
    @Mock private SummonProbabilityRepository probabilityRepository;
    @Mock private SummonProbabilityDefinitionRepository definitionRepository;
    @Mock private SummonHeroRepository summonHeroRepository;
    @Mock private SummonBonusRuleRepository bonusRuleRepository;
    @Mock private SummonBonusStepRepository bonusStepRepository;
    @Mock private SummonBonusStepProbabilityRepository bonusStepProbabilityRepository;
    @Mock private SummonBonusStepHeroRepository bonusStepHeroRepository;
    @Mock private HeroRepository heroRepository;
    @Mock private RarityRepository rarityRepository;
    @Mock private MediaUrlResolver mediaUrlResolver;

    private SummonSimulationService service;

    @BeforeEach
    void setUp() {
        service = new SummonSimulationService(
                summonRepository,
                variantRepository,
                probabilityRepository,
                definitionRepository,
                summonHeroRepository,
                bonusRuleRepository,
                bonusStepRepository,
                bonusStepProbabilityRepository,
                bonusStepHeroRepository,
                heroRepository,
                rarityRepository,
                mediaUrlResolver,
                bound -> 0
        );
    }

    @Test
    void shouldApplyIndependentBonusToBaseAndConditionalResultsOnly() {
        UUID summonId = UUID.randomUUID();
        UUID variantId = UUID.randomUUID();
        UUID probabilityId = UUID.randomUUID();
        UUID probabilityDefinitionId = UUID.randomUUID();
        UUID conditionalRuleId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID independentRuleId = UUID.fromString("00000000-0000-0000-0000-000000000002");
        UUID conditionalStepId = UUID.randomUUID();
        UUID independentStepId = UUID.randomUUID();

        when(summonRepository.findById(summonId)).thenReturn(Optional.of(Summon.builder()
                .id(summonId)
                .status(ContentStatus.READY)
                .build()));
        when(variantRepository.findByIdAndSummonId(variantId, summonId)).thenReturn(Optional.of(
                SummonVariant.builder()
                        .id(variantId)
                        .summonId(summonId)
                        .version(0)
                        .variantType(SummonVariantType.DEFAULT)
                        .status(ContentStatus.READY)
                        .build()
        ));
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(variantId)).thenReturn(List.of(
                SummonProbability.builder()
                        .id(probabilityId)
                        .variantId(variantId)
                        .definitionId(probabilityDefinitionId)
                        .kind(SummonProbabilityKind.MAIN)
                        .valuePercent(new BigDecimal("100.00"))
                        .position(1)
                        .build()
        ));
        when(summonHeroRepository.findAllByIdProbabilityIdIn(List.of(probabilityId))).thenReturn(List.of(
                SummonHero.builder().id(new SummonHeroId(probabilityId, 10L)).build()
        ));

        SummonBonusRule conditionalRule = SummonBonusRule.builder()
                .id(conditionalRuleId)
                .variantId(variantId)
                .definitionId(UUID.randomUUID())
                .kind(SummonBonusKind.CONDITIONAL)
                .triggerConditionType(BonusResultConditionType.ANY_RESULT)
                .build();
        SummonBonusRule independentRule = SummonBonusRule.builder()
                .id(independentRuleId)
                .variantId(variantId)
                .definitionId(UUID.randomUUID())
                .kind(SummonBonusKind.INDEPENDENT)
                .build();
        when(bonusRuleRepository.findAllByVariantId(variantId)).thenReturn(List.of(
                independentRule,
                conditionalRule
        ));
        when(bonusStepRepository.findAllByBonusRuleIdInOrderByPositionAsc(any())).thenReturn(List.of(
                SummonBonusStep.builder()
                        .id(conditionalStepId)
                        .bonusRuleId(conditionalRuleId)
                        .position(1)
                        .activationChancePercent(new BigDecimal("100.00"))
                        .rewardMode(BonusRewardMode.MAIN_ROLL)
                        .build(),
                SummonBonusStep.builder()
                        .id(independentStepId)
                        .bonusRuleId(independentRuleId)
                        .position(1)
                        .activationChancePercent(new BigDecimal("100.00"))
                        .rewardMode(BonusRewardMode.SPECIFIC_HERO_POOL)
                        .build()
        ));
        when(bonusStepProbabilityRepository.findAllByIdBonusStepIdIn(any())).thenReturn(List.of());
        when(bonusStepHeroRepository.findAllByIdBonusStepIdIn(any())).thenReturn(List.of(
                SummonBonusStepHero.builder()
                        .id(new SummonBonusStepHeroId(independentStepId, 20L))
                        .build()
        ));
        when(definitionRepository.findAllById(any())).thenReturn(List.of());
        when(heroRepository.findAllById(any())).thenReturn(List.of(
                Hero.builder().id(10L).slug("base-hero").rarityId(5L).status(ContentStatus.DRAFT).build(),
                Hero.builder().id(20L).slug("bonus-hero").rarityId(5L).status(ContentStatus.ARCHIVED).build()
        ));
        when(rarityRepository.findAllById(any())).thenReturn(List.of(
                Rarity.builder().id(5L).stars(5).build()
        ));

        SummonSimulationResponse response = service.simulate(
                summonId,
                variantId,
                new SummonSimulationRequest(0, 1, List.of(), List.of(), List.of())
        );

        assertThat(response.results())
                .extracting(result -> result.source())
                .containsExactly(
                        SummonSimulationResultSource.BASE,
                        SummonSimulationResultSource.CONDITIONAL_BONUS,
                        SummonSimulationResultSource.INDEPENDENT_BONUS,
                        SummonSimulationResultSource.INDEPENDENT_BONUS
                );
        assertThat(response.results().get(2).parentResultSequence()).isEqualTo(1);
        assertThat(response.results().get(3).parentResultSequence()).isEqualTo(2);
        assertThat(response.totalResultCount()).isEqualTo(4);
    }
}
