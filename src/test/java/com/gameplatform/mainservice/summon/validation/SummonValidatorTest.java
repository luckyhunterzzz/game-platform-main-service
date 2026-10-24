package com.gameplatform.mainservice.summon.validation;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.media.validation.ImageReferenceValidator;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbabilityDefinition;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusConditionRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusRuleCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusStepCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonBonusStepProbabilityCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityCreateRequest;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummonValidatorTest {

    @Mock
    private SummonRepository summonRepository;

    @Mock
    private SummonProbabilityDefinitionRepository probabilityDefinitionRepository;

    @Mock
    private HeroRepository heroRepository;

    @Mock
    private RarityRepository rarityRepository;

    @Mock
    private ImageReferenceValidator imageReferenceValidator;

    private SummonValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SummonValidator(
                summonRepository,
                probabilityDefinitionRepository,
                heroRepository,
                rarityRepository,
                imageReferenceValidator
        );

        when(probabilityDefinitionRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<UUID> ids = invocation.getArgument(0);
            List<SummonProbabilityDefinition> definitions = new ArrayList<>();
            ids.forEach(id -> definitions.add(SummonProbabilityDefinition.builder().id(id).build()));
            return definitions;
        });
    }

    @Test
    void shouldAcceptReadySummonWithCompleteMainDistributionAndConditionalBonus() {
        UUID rareDefinitionId = UUID.randomUUID();
        UUID legendaryDefinitionId = UUID.randomUUID();
        UUID bonusDefinitionId = UUID.randomUUID();

        SummonCreateRequest request = request(
                ContentStatus.READY,
                List.of(
                        probability(rareDefinitionId, "70", 1),
                        probability(legendaryDefinitionId, "30", 2)
                ),
                List.of(new SummonBonusRuleCreateRequest(
                        bonusDefinitionId,
                        SummonBonusKind.CONDITIONAL,
                        true,
                        new SummonBonusConditionRequest(
                                BonusResultConditionType.PROBABILITY,
                                null,
                                legendaryDefinitionId
                        ),
                        null,
                        List.of(new SummonBonusStepCreateRequest(
                                1,
                                new BigDecimal("30"),
                                BonusRewardMode.CUSTOM_DISTRIBUTION,
                                List.of(
                                        new SummonBonusStepProbabilityCreateRequest(
                                                legendaryDefinitionId,
                                                new BigDecimal("30"),
                                                1
                                        ),
                                        new SummonBonusStepProbabilityCreateRequest(
                                                rareDefinitionId,
                                                new BigDecimal("70"),
                                                2
                                        )
                                ),
                                false,
                                List.of()
                        ))
                ))
        );

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectReadySummonWhenMainProbabilitiesDoNotTotalOneHundred() {
        UUID definitionId = UUID.randomUUID();
        SummonCreateRequest request = request(
                ContentStatus.READY,
                List.of(probability(definitionId, "99.9", 1)),
                List.of()
        );

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("MAIN probabilities must total 100% for a READY summon");
    }

    @Test
    void shouldRequireContinuationConditionForMultipleBonusSteps() {
        UUID probabilityDefinitionId = UUID.randomUUID();
        UUID bonusDefinitionId = UUID.randomUUID();

        SummonCreateRequest request = request(
                ContentStatus.DRAFT,
                List.of(probability(probabilityDefinitionId, "100", 1)),
                List.of(new SummonBonusRuleCreateRequest(
                        bonusDefinitionId,
                        SummonBonusKind.CONDITIONAL,
                        false,
                        new SummonBonusConditionRequest(
                                BonusResultConditionType.ANY_RESULT,
                                null,
                                null
                        ),
                        null,
                        List.of(
                                new SummonBonusStepCreateRequest(
                                        1,
                                        new BigDecimal("30"),
                                        BonusRewardMode.MAIN_ROLL,
                                        List.of(),
                                        false,
                                        List.of()
                                ),
                                new SummonBonusStepCreateRequest(
                                        2,
                                        new BigDecimal("20"),
                                        BonusRewardMode.MAIN_ROLL,
                                        List.of(),
                                        false,
                                        List.of()
                                )
                        )
                ))
        );

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("bonus with multiple steps requires continuationCondition");
    }

    @Test
    void shouldRejectIncompleteCustomBonusDistribution() {
        UUID rareDefinitionId = UUID.randomUUID();
        UUID legendaryDefinitionId = UUID.randomUUID();
        UUID bonusDefinitionId = UUID.randomUUID();

        SummonCreateRequest request = request(
                ContentStatus.DRAFT,
                List.of(
                        probability(rareDefinitionId, "70", 1),
                        probability(legendaryDefinitionId, "30", 2)
                ),
                List.of(new SummonBonusRuleCreateRequest(
                        bonusDefinitionId,
                        SummonBonusKind.CONDITIONAL,
                        false,
                        new SummonBonusConditionRequest(
                                BonusResultConditionType.PROBABILITY,
                                null,
                                legendaryDefinitionId
                        ),
                        null,
                        List.of(new SummonBonusStepCreateRequest(
                                1,
                                new BigDecimal("100"),
                                BonusRewardMode.CUSTOM_DISTRIBUTION,
                                List.of(
                                        new SummonBonusStepProbabilityCreateRequest(
                                                legendaryDefinitionId,
                                                new BigDecimal("30"),
                                                1
                                        ),
                                        new SummonBonusStepProbabilityCreateRequest(
                                                rareDefinitionId,
                                                new BigDecimal("35"),
                                                2
                                        )
                                ),
                                false,
                                List.of()
                        ))
                ))
        );

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("bonus step reward probabilities must total 100%");
    }

    @Test
    void shouldAcceptIndependentBonusWithUserSelectedHero() {
        UUID probabilityDefinitionId = UUID.randomUUID();
        UUID bonusDefinitionId = UUID.randomUUID();

        SummonCreateRequest request = request(
                ContentStatus.READY,
                List.of(probability(probabilityDefinitionId, "100", 1)),
                List.of(new SummonBonusRuleCreateRequest(
                        bonusDefinitionId,
                        SummonBonusKind.INDEPENDENT,
                        true,
                        null,
                        null,
                        List.of(new SummonBonusStepCreateRequest(
                                1,
                                new BigDecimal("1.30"),
                                BonusRewardMode.SPECIFIC_HERO_POOL,
                                List.of(),
                                true,
                                List.of()
                        ))
                ))
        );

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptIndependentBonusWithFixedFiveStarHero() {
        UUID probabilityDefinitionId = UUID.randomUUID();
        UUID bonusDefinitionId = UUID.randomUUID();
        when(heroRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            return ids.iterator().hasNext()
                    ? List.of(Hero.builder().id(42L).rarityId(7L).build())
                    : List.of();
        });
        when(rarityRepository.findAllById(any())).thenReturn(List.of(Rarity.builder()
                .id(7L)
                .stars(5)
                .build()));

        SummonCreateRequest request = request(
                ContentStatus.READY,
                List.of(probability(probabilityDefinitionId, "100", 1)),
                List.of(new SummonBonusRuleCreateRequest(
                        bonusDefinitionId,
                        SummonBonusKind.INDEPENDENT,
                        true,
                        null,
                        null,
                        List.of(new SummonBonusStepCreateRequest(
                                1,
                                new BigDecimal("1.30"),
                                BonusRewardMode.SPECIFIC_HERO_POOL,
                                List.of(),
                                false,
                                List.of(42L)
                        ))
                ))
        );

        assertThatCode(() -> validator.validateCreate(request)).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectNonFiveStarHeroInRecommendedPool() {
        UUID definitionId = UUID.randomUUID();
        SummonProbabilityCreateRequest probability = new SummonProbabilityCreateRequest(
                definitionId,
                SummonProbabilityKind.MAIN,
                new BigDecimal("100"),
                1,
                List.of(42L),
                true,
                2
        );
        when(heroRepository.findAllById(any())).thenReturn(List.of(Hero.builder()
                .id(42L)
                .rarityId(7L)
                .build()));
        when(rarityRepository.findAllById(any())).thenReturn(List.of(Rarity.builder()
                .id(7L)
                .stars(4)
                .build()));

        assertThatThrownBy(() -> validator.validateCreate(request(
                ContentStatus.READY,
                List.of(probability),
                List.of()
        )))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("recommended hero pool may contain only 5-star heroes");
    }

    private SummonCreateRequest request(
            ContentStatus status,
            List<SummonProbabilityCreateRequest> probabilities,
            List<SummonBonusRuleCreateRequest> bonusRules
    ) {
        return new SummonCreateRequest(
                "astral-summon",
                new LocalizedTextJson("Астральный призыв", "Astral Summon"),
                null,
                status,
                null,
                null,
                null,
                null,
                probabilities,
                bonusRules
        );
    }

    private SummonProbabilityCreateRequest probability(UUID definitionId, String value, int position) {
        return new SummonProbabilityCreateRequest(
                definitionId,
                SummonProbabilityKind.MAIN,
                new BigDecimal(value),
                position,
                List.of(),
                false,
                null
        );
    }
}
