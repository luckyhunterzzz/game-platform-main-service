package com.gameplatform.mainservice.summon.validation;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
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
                                BonusRewardMode.SPECIFIC_PROBABILITY,
                                legendaryDefinitionId
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
                                        null
                                ),
                                new SummonBonusStepCreateRequest(
                                        2,
                                        new BigDecimal("20"),
                                        BonusRewardMode.MAIN_ROLL,
                                        null
                                )
                        )
                ))
        );

        assertThatThrownBy(() -> validator.validateCreate(request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("bonus with multiple steps requires continuationCondition");
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
                List.of()
        );
    }
}
