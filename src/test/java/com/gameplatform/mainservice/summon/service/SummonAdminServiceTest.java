package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.dto.json.LocalizedTextJson;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.security.CurrentUserProvider;
import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusRule;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStep;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonBonusStepProbabilityId;
import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonHeroId;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.summon.domain.enums.BonusResultConditionType;
import com.gameplatform.mainservice.summon.domain.enums.BonusRewardMode;
import com.gameplatform.mainservice.summon.domain.enums.SummonBonusKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonProbabilityKind;
import com.gameplatform.mainservice.summon.domain.enums.SummonHeroVariantMode;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import com.gameplatform.mainservice.summon.dto.request.SummonVariantCloneRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonCreateRequest;
import com.gameplatform.mainservice.summon.dto.request.SummonProbabilityCreateRequest;
import com.gameplatform.mainservice.summon.dto.response.SummonAdminDetailsResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonAdminSummaryResponse;
import com.gameplatform.mainservice.summon.repository.SummonBonusRuleRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonBonusStepProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import com.gameplatform.mainservice.summon.repository.SummonVariantRepository;
import com.gameplatform.mainservice.summon.validation.SummonValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummonAdminServiceTest {

    @Mock
    private SummonRepository summonRepository;
    @Mock
    private SummonVariantRepository summonVariantRepository;
    @Mock
    private SummonProbabilityDefinitionRepository probabilityDefinitionRepository;
    @Mock
    private SummonProbabilityRepository probabilityRepository;
    @Mock
    private SummonHeroRepository summonHeroRepository;
    @Mock
    private SummonBonusRuleRepository bonusRuleRepository;
    @Mock
    private SummonBonusStepRepository bonusStepRepository;
    @Mock
    private SummonBonusStepHeroRepository bonusStepHeroRepository;
    @Mock
    private SummonBonusStepProbabilityRepository bonusStepProbabilityRepository;
    @Mock
    private HeroRepository heroRepository;
    @Mock
    private SummonValidator summonValidator;
    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private MediaUrlResolver mediaUrlResolver;

    private SummonAdminService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-10-18T10:00:00Z"), ZoneOffset.UTC);
        service = new SummonAdminService(
                summonRepository,
                summonVariantRepository,
                probabilityDefinitionRepository,
                probabilityRepository,
                summonHeroRepository,
                bonusRuleRepository,
                bonusStepRepository,
                bonusStepHeroRepository,
                bonusStepProbabilityRepository,
                heroRepository,
                summonValidator,
                currentUserProvider,
                mediaUrlResolver,
                clock
        );
    }

    @Test
    void shouldReturnSummonCatalogInRepositoryOrder() {
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-10-18T09:00:00Z");
        Summon summon = Summon.builder()
                .id(UUID.randomUUID())
                .slug("astral-summon")
                .nameJson(new LocalizedTextJson("Астральный призыв", "Astral Summon"))
                .descriptionJson(new LocalizedTextJson("Описание", "Description"))
                .status(ContentStatus.READY)
                .imageBucket("media")
                .imageObjectKey("summons/astral.png")
                .backgroundImageBucket("media")
                .backgroundImageObjectKey("summons/astral-background.png")
                .updatedAt(updatedAt)
                .build();

        when(summonRepository.findAllByOrderByUpdatedAtDesc()).thenReturn(List.of(summon));
        when(mediaUrlResolver.resolveUrl("media", "summons/astral.png"))
                .thenReturn("https://cdn.test/astral.png");
        when(mediaUrlResolver.resolveUrl("media", "summons/astral-background.png"))
                .thenReturn("https://cdn.test/astral-background.png");

        List<SummonAdminSummaryResponse> result = service.getAll();

        assertThat(result).containsExactly(new SummonAdminSummaryResponse(
                summon.getId(),
                "astral-summon",
                summon.getNameJson(),
                summon.getDescriptionJson(),
                ContentStatus.READY,
                "https://cdn.test/astral.png",
                "https://cdn.test/astral-background.png",
                updatedAt
        ));
    }

    @Test
    void shouldReturnDefaultAndScheduledVariantsInSummonDetails() {
        UUID summonId = UUID.randomUUID();
        UUID defaultVariantId = UUID.randomUUID();
        UUID laterVariantId = UUID.randomUUID();
        UUID earlierVariantId = UUID.randomUUID();
        Summon summon = Summon.builder()
                .id(summonId)
                .slug("astral-summon")
                .nameJson(new LocalizedTextJson("Астральный призыв", "Astral Summon"))
                .status(ContentStatus.READY)
                .build();
        SummonVariant defaultVariant = SummonVariant.builder()
                .id(defaultVariantId)
                .summonId(summonId)
                .variantType(SummonVariantType.DEFAULT)
                .status(ContentStatus.READY)
                .build();
        SummonVariant laterVariant = SummonVariant.builder()
                .id(laterVariantId)
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(OffsetDateTime.parse("2026-10-25T00:00:00Z"))
                .endsAt(OffsetDateTime.parse("2026-10-26T00:00:00Z"))
                .status(ContentStatus.DRAFT)
                .build();
        SummonVariant earlierVariant = SummonVariant.builder()
                .id(earlierVariantId)
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(OffsetDateTime.parse("2026-10-20T00:00:00Z"))
                .endsAt(OffsetDateTime.parse("2026-10-21T00:00:00Z"))
                .status(ContentStatus.DRAFT)
                .build();

        when(summonRepository.findById(summonId)).thenReturn(Optional.of(summon));
        when(summonVariantRepository.findFirstBySummonIdAndVariantType(
                summonId,
                SummonVariantType.DEFAULT
        )).thenReturn(Optional.of(defaultVariant));
        when(summonVariantRepository.findAllBySummonIdAndVariantTypeAndEndsAtAfterOrderByStartsAtAsc(
                summonId,
                SummonVariantType.SCHEDULED,
                OffsetDateTime.parse("2026-10-18T10:00:00Z")
        )).thenReturn(List.of(earlierVariant, laterVariant));
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(any()))
                .thenReturn(List.of());
        when(bonusRuleRepository.findAllByVariantId(any())).thenReturn(List.of());

        SummonAdminDetailsResponse result = service.getById(summonId);

        assertThat(result.defaultVariant().id()).isEqualTo(defaultVariantId);
        assertThat(result.scheduledVariants())
                .extracting(variant -> variant.id())
                .containsExactly(earlierVariantId, laterVariantId);
    }

    @Test
    void shouldReturnPastVariantsFromNewestToOldestInPagesOfTen() {
        UUID summonId = UUID.randomUUID();
        UUID newerVariantId = UUID.randomUUID();
        UUID olderVariantId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.parse("2026-10-18T10:00:00Z");
        SummonVariant newerVariant = SummonVariant.builder()
                .id(newerVariantId)
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(OffsetDateTime.parse("2026-10-17T00:00:00Z"))
                .endsAt(OffsetDateTime.parse("2026-10-18T09:00:00Z"))
                .status(ContentStatus.READY)
                .build();
        SummonVariant olderVariant = SummonVariant.builder()
                .id(olderVariantId)
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(OffsetDateTime.parse("2026-10-10T00:00:00Z"))
                .endsAt(OffsetDateTime.parse("2026-10-11T00:00:00Z"))
                .status(ContentStatus.READY)
                .build();
        PageRequest pageable = PageRequest.of(0, 10);

        when(summonRepository.existsById(summonId)).thenReturn(true);
        when(summonVariantRepository
                .findAllBySummonIdAndVariantTypeAndEndsAtLessThanEqualOrderByEndsAtDesc(
                        summonId,
                        SummonVariantType.SCHEDULED,
                        now,
                        pageable
                )).thenReturn(new PageImpl<>(List.of(newerVariant, olderVariant), pageable, 12));
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(any()))
                .thenReturn(List.of());
        when(bonusRuleRepository.findAllByVariantId(any())).thenReturn(List.of());

        var result = service.getVariantHistory(summonId, 0);

        assertThat(result.size()).isEqualTo(10);
        assertThat(result.totalElements()).isEqualTo(12);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.items())
                .extracting(variant -> variant.id())
                .containsExactly(newerVariantId, olderVariantId);
    }

    @Test
    void shouldDeepCloneVariantAndRemapInternalReferences() {
        UUID summonId = UUID.randomUUID();
        UUID sourceVariantId = UUID.randomUUID();
        UUID targetVariantId = UUID.randomUUID();
        UUID sourceProbabilityId = UUID.randomUUID();
        UUID targetProbabilityId = UUID.randomUUID();
        UUID definitionId = UUID.randomUUID();
        UUID sourceRuleId = UUID.randomUUID();
        UUID targetRuleId = UUID.randomUUID();
        UUID sourceStepId = UUID.randomUUID();
        UUID targetStepId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        SummonVariant sourceVariant = SummonVariant.builder()
                .id(sourceVariantId)
                .summonId(summonId)
                .variantType(SummonVariantType.DEFAULT)
                .status(ContentStatus.READY)
                .build();
        SummonProbability sourceProbability = SummonProbability.builder()
                .id(sourceProbabilityId)
                .variantId(sourceVariantId)
                .definitionId(definitionId)
                .kind(SummonProbabilityKind.MAIN)
                .valuePercent(new BigDecimal("100"))
                .position(1)
                .heroVariantMode(SummonHeroVariantMode.LATEST_COSTUME)
                .build();
        SummonBonusRule sourceRule = SummonBonusRule.builder()
                .id(sourceRuleId)
                .variantId(sourceVariantId)
                .definitionId(definitionId)
                .kind(SummonBonusKind.CONDITIONAL)
                .userSelectable(true)
                .triggerConditionType(BonusResultConditionType.PROBABILITY)
                .triggerProbabilityId(sourceProbabilityId)
                .continuationConditionType(BonusResultConditionType.ANY_RESULT)
                .build();
        SummonBonusStep sourceStep = SummonBonusStep.builder()
                .id(sourceStepId)
                .bonusRuleId(sourceRuleId)
                .position(1)
                .activationChancePercent(new BigDecimal("30"))
                .rewardMode(BonusRewardMode.CUSTOM_DISTRIBUTION)
                .build();
        SummonBonusStepProbability sourceStepProbability = SummonBonusStepProbability.builder()
                .id(new SummonBonusStepProbabilityId(sourceStepId, sourceProbabilityId))
                .valuePercent(new BigDecimal("100"))
                .position(1)
                .build();

        when(summonRepository.existsById(summonId)).thenReturn(true);
        when(summonVariantRepository.findByIdAndSummonId(sourceVariantId, summonId))
                .thenReturn(Optional.of(sourceVariant));
        when(currentUserProvider.getUserId()).thenReturn(actorId);
        when(summonVariantRepository.save(any())).thenAnswer(invocation -> {
            SummonVariant variant = invocation.getArgument(0);
            variant.setId(targetVariantId);
            return variant;
        });
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(sourceVariantId))
                .thenReturn(List.of(sourceProbability));
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(targetVariantId))
                .thenReturn(List.of());
        when(probabilityRepository.save(any())).thenAnswer(invocation -> {
            SummonProbability probability = invocation.getArgument(0);
            probability.setId(targetProbabilityId);
            return probability;
        });
        when(summonHeroRepository.findAllByIdProbabilityIdIn(List.of(sourceProbabilityId)))
                .thenReturn(List.of(new SummonHero(new SummonHeroId(sourceProbabilityId, 42L))));
        when(bonusRuleRepository.findAllByVariantId(sourceVariantId)).thenReturn(List.of(sourceRule));
        when(bonusRuleRepository.findAllByVariantId(targetVariantId)).thenReturn(List.of());
        when(bonusRuleRepository.save(any())).thenAnswer(invocation -> {
            SummonBonusRule rule = invocation.getArgument(0);
            rule.setId(targetRuleId);
            return rule;
        });
        when(bonusStepRepository.findAllByBonusRuleIdInOrderByPositionAsc(List.of(sourceRuleId)))
                .thenReturn(List.of(sourceStep));
        when(bonusStepRepository.save(any())).thenAnswer(invocation -> {
            SummonBonusStep step = invocation.getArgument(0);
            step.setId(targetStepId);
            return step;
        });
        when(bonusStepProbabilityRepository.findAllByIdBonusStepIdIn(List.of(sourceStepId)))
                .thenReturn(List.of(sourceStepProbability));

        service.cloneVariant(
                summonId,
                new SummonVariantCloneRequest(
                        sourceVariantId,
                        OffsetDateTime.parse("2026-10-20T00:00:00Z"),
                        OffsetDateTime.parse("2026-10-25T00:00:00Z"),
                        ContentStatus.DRAFT
                )
        );

        ArgumentCaptor<Iterable<SummonHero>> heroesCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(summonHeroRepository).saveAll(heroesCaptor.capture());
        SummonHero clonedHero = heroesCaptor.getValue().iterator().next();
        assertThat(clonedHero.getId())
                .isEqualTo(new SummonHeroId(targetProbabilityId, 42L));

        ArgumentCaptor<SummonProbability> probabilityCaptor = ArgumentCaptor.forClass(SummonProbability.class);
        verify(probabilityRepository).save(probabilityCaptor.capture());
        assertThat(probabilityCaptor.getValue().getHeroVariantMode())
                .isEqualTo(SummonHeroVariantMode.LATEST_COSTUME);

        ArgumentCaptor<SummonBonusRule> ruleCaptor = ArgumentCaptor.forClass(SummonBonusRule.class);
        verify(bonusRuleRepository).save(ruleCaptor.capture());
        assertThat(ruleCaptor.getValue().getVariantId()).isEqualTo(targetVariantId);
        assertThat(ruleCaptor.getValue().getTriggerProbabilityId()).isEqualTo(targetProbabilityId);

        ArgumentCaptor<SummonBonusStep> stepCaptor = ArgumentCaptor.forClass(SummonBonusStep.class);
        verify(bonusStepRepository).save(stepCaptor.capture());
        SummonBonusStep clonedStep = stepCaptor.getValue();
        assertThat(clonedStep.getBonusRuleId()).isEqualTo(targetRuleId);

        ArgumentCaptor<Iterable<SummonBonusStepProbability>> stepProbabilitiesCaptor =
                ArgumentCaptor.forClass(Iterable.class);
        verify(bonusStepProbabilityRepository).saveAll(stepProbabilitiesCaptor.capture());
        SummonBonusStepProbability clonedStepProbability = stepProbabilitiesCaptor
                .getValue()
                .iterator()
                .next();
        assertThat(clonedStepProbability.getId())
                .isEqualTo(new SummonBonusStepProbabilityId(targetStepId, targetProbabilityId));
    }

    @Test
    void shouldResolveOnlyLatestCostumeForEachBaseHero() {
        Hero originalOne = Hero.builder().id(1L).isCostume(false).build();
        Hero originalTwo = Hero.builder().id(2L).isCostume(false).build();
        Hero firstCostume = Hero.builder()
                .id(11L)
                .isCostume(true)
                .baseHeroId(1L)
                .costumeIndex(1)
                .build();
        Hero latestCostume = Hero.builder()
                .id(12L)
                .isCostume(true)
                .baseHeroId(1L)
                .costumeIndex(2)
                .build();
        when(heroRepository.findAllById(any()))
                .thenReturn(List.of(originalOne, latestCostume, originalTwo), List.of(originalOne, originalTwo));
        when(heroRepository.findAllByBaseHeroIdIn(List.of(1L, 2L)))
                .thenReturn(List.of(firstCostume, latestCostume));

        SummonProbabilityCreateRequest request = new SummonProbabilityCreateRequest(
                UUID.randomUUID(),
                SummonProbabilityKind.MAIN,
                new BigDecimal("100"),
                1,
                List.of(1L, 12L, 2L),
                false,
                null,
                SummonHeroVariantMode.LATEST_COSTUME
        );

        assertThat(service.resolveHeroIds(request)).containsExactly(12L, 2L);
    }

    @Test
    void shouldResolveOriginalHeroAndDeduplicateCostumes() {
        Hero originalOne = Hero.builder().id(1L).isCostume(false).build();
        Hero originalTwo = Hero.builder().id(2L).isCostume(false).build();
        Hero costume = Hero.builder()
                .id(12L)
                .isCostume(true)
                .baseHeroId(1L)
                .costumeIndex(2)
                .build();
        when(heroRepository.findAllById(any()))
                .thenReturn(List.of(costume, originalTwo), List.of(originalOne, originalTwo));

        SummonProbabilityCreateRequest request = new SummonProbabilityCreateRequest(
                UUID.randomUUID(),
                SummonProbabilityKind.MAIN,
                new BigDecimal("100"),
                1,
                List.of(12L, 2L),
                false,
                null,
                SummonHeroVariantMode.ORIGINAL
        );

        assertThat(service.resolveHeroIds(request)).containsExactly(1L, 2L);
    }

    @Test
    void shouldUpdateSummonAndKeepScheduledVariants() {
        UUID summonId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        Summon summon = Summon.builder()
                .id(summonId)
                .slug("old-summon")
                .nameJson(new LocalizedTextJson("Старый", "Old"))
                .status(ContentStatus.DRAFT)
                .build();
        SummonVariant defaultVariant = SummonVariant.builder()
                .id(UUID.randomUUID())
                .summonId(summonId)
                .variantType(SummonVariantType.DEFAULT)
                .status(ContentStatus.DRAFT)
                .build();
        SummonVariant scheduledVariant = SummonVariant.builder()
                .id(UUID.randomUUID())
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(OffsetDateTime.parse("2026-10-20T00:00:00Z"))
                .endsAt(OffsetDateTime.parse("2026-10-21T00:00:00Z"))
                .status(ContentStatus.DRAFT)
                .build();
        SummonCreateRequest request = new SummonCreateRequest(
                "updated-summon",
                new LocalizedTextJson("Обновлённый", "Updated"),
                null,
                ContentStatus.DRAFT,
                null,
                null,
                null,
                null,
                List.of(),
                List.of()
        );

        when(summonRepository.findById(summonId)).thenReturn(Optional.of(summon));
        when(summonVariantRepository.findFirstBySummonIdAndVariantType(
                summonId,
                SummonVariantType.DEFAULT
        )).thenReturn(Optional.of(defaultVariant));
        when(summonVariantRepository.findAllBySummonIdAndVariantTypeAndEndsAtAfterOrderByStartsAtAsc(
                summonId,
                SummonVariantType.SCHEDULED,
                OffsetDateTime.parse("2026-10-18T10:00:00Z")
        )).thenReturn(List.of(scheduledVariant));
        when(currentUserProvider.getUserId()).thenReturn(actorId);
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(any())).thenReturn(List.of());
        when(bonusRuleRepository.findAllByVariantId(any())).thenReturn(List.of());

        SummonAdminDetailsResponse result = service.update(summonId, request);

        verify(summonValidator).validateUpdate(summonId, request);
        assertThat(summon.getSlug()).isEqualTo("updated-summon");
        assertThat(summon.getUpdatedBy()).isEqualTo(actorId);
        assertThat(defaultVariant.getUpdatedBy()).isEqualTo(actorId);
        assertThat(result.defaultVariant().id()).isEqualTo(defaultVariant.getId());
        assertThat(result.scheduledVariants())
                .extracting(variant -> variant.id())
                .containsExactly(scheduledVariant.getId());
    }
}
