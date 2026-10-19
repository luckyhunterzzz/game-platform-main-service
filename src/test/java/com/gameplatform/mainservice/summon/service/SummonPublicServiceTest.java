package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import com.gameplatform.mainservice.summon.dto.response.SummonCalendarFeedResponse;
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

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummonPublicServiceTest {

    @Mock
    private SummonRepository summonRepository;
    @Mock
    private SummonVariantRepository variantRepository;
    @Mock
    private SummonProbabilityRepository probabilityRepository;
    @Mock
    private SummonProbabilityDefinitionRepository definitionRepository;
    @Mock
    private SummonHeroRepository summonHeroRepository;
    @Mock
    private HeroRepository heroRepository;
    @Mock
    private RarityRepository rarityRepository;
    @Mock
    private MediaUrlResolver mediaUrlResolver;

    private SummonPublicService service;

    @BeforeEach
    void setUp() {
        service = new SummonPublicService(
                summonRepository,
                variantRepository,
                probabilityRepository,
                definitionRepository,
                summonHeroRepository,
                heroRepository,
                rarityRepository,
                mediaUrlResolver,
                Clock.fixed(Instant.parse("2026-10-20T12:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void shouldSplitCurrentAndUpcomingAndKeepArchivedParentHistoryAvailable() {
        UUID summonId = UUID.randomUUID();
        Summon summon = Summon.builder()
                .id(summonId)
                .slug("astral-summon")
                .status(ContentStatus.ARCHIVED)
                .build();
        SummonVariant current = scheduled(
                summonId,
                "2026-10-20T10:00:00Z",
                "2026-10-20T14:00:00Z"
        );
        SummonVariant nearFuture = scheduled(
                summonId,
                "2026-10-21T10:00:00Z",
                "2026-10-21T14:00:00Z"
        );
        SummonVariant farFuture = scheduled(
                summonId,
                "2026-10-25T10:00:00Z",
                "2026-10-25T14:00:00Z"
        );

        when(variantRepository.findAllByVariantTypeAndStatus(
                SummonVariantType.SCHEDULED,
                ContentStatus.READY
        )).thenReturn(List.of(nearFuture, current, farFuture));
        when(summonRepository.findAllById(any())).thenReturn(List.of(summon));
        when(probabilityRepository.findAllByVariantIdOrderByPositionAsc(any())).thenReturn(List.of());
        when(definitionRepository.findAllById(any())).thenReturn(List.of());

        SummonCalendarFeedResponse result = service.getCalendar();

        assertThat(result.current())
                .extracting(item -> item.variant().id())
                .containsExactly(current.getId());
        assertThat(result.upcoming())
                .extracting(item -> item.variant().id())
                .containsExactly(farFuture.getId(), nearFuture.getId());
    }

    private SummonVariant scheduled(UUID summonId, String startsAt, String endsAt) {
        return SummonVariant.builder()
                .id(UUID.randomUUID())
                .summonId(summonId)
                .variantType(SummonVariantType.SCHEDULED)
                .startsAt(OffsetDateTime.parse(startsAt))
                .endsAt(OffsetDateTime.parse(endsAt))
                .status(ContentStatus.READY)
                .build();
    }
}
