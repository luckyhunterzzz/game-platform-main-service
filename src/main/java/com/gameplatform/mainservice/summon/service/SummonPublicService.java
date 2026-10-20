package com.gameplatform.mainservice.summon.service;

import com.gameplatform.mainservice.common.domain.enums.ContentStatus;
import com.gameplatform.mainservice.exception.exceptions.NotFoundException;
import com.gameplatform.mainservice.hero.domain.entity.Hero;
import com.gameplatform.mainservice.hero.domain.entity.Rarity;
import com.gameplatform.mainservice.hero.dto.response.CatalogPageResponse;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.hero.repository.RarityRepository;
import com.gameplatform.mainservice.publication.resolver.MediaUrlResolver;
import com.gameplatform.mainservice.summon.domain.entity.Summon;
import com.gameplatform.mainservice.summon.domain.entity.SummonHero;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbability;
import com.gameplatform.mainservice.summon.domain.entity.SummonProbabilityDefinition;
import com.gameplatform.mainservice.summon.domain.entity.SummonVariant;
import com.gameplatform.mainservice.summon.domain.enums.SummonVariantType;
import com.gameplatform.mainservice.summon.dto.response.SummonCalendarFeedResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonHeroPreviewResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonProbabilityDefinitionResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonPublicProbabilityResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonPublicResponse;
import com.gameplatform.mainservice.summon.dto.response.SummonPublicVariantResponse;
import com.gameplatform.mainservice.summon.repository.SummonHeroRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityDefinitionRepository;
import com.gameplatform.mainservice.summon.repository.SummonProbabilityRepository;
import com.gameplatform.mainservice.summon.repository.SummonRepository;
import com.gameplatform.mainservice.summon.repository.SummonVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
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
public class SummonPublicService {

    private static final int DEFAULT_HISTORY_PAGE_SIZE = 12;
    private static final int MAX_HISTORY_PAGE_SIZE = 50;

    private final SummonRepository summonRepository;
    private final SummonVariantRepository variantRepository;
    private final SummonProbabilityRepository probabilityRepository;
    private final SummonProbabilityDefinitionRepository definitionRepository;
    private final SummonHeroRepository summonHeroRepository;
    private final HeroRepository heroRepository;
    private final RarityRepository rarityRepository;
    private final MediaUrlResolver mediaUrlResolver;
    private final Clock clock;

    @Transactional(readOnly = true)
    public List<SummonPublicResponse> getBaseSummons() {
        return summonRepository.findAllByStatusOrderByUpdatedAtDesc(ContentStatus.READY).stream()
                .map(summon -> findDefaultVariant(summon.getId())
                        .map(variant -> toPublicResponse(summon, variant))
                        .orElse(null))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional(readOnly = true)
    public SummonPublicResponse getBySlug(String slug) {
        Summon summon = summonRepository.findBySlugAndStatus(
                        slug.trim().toLowerCase(Locale.ROOT),
                        ContentStatus.READY
                )
                .orElseThrow(() -> new NotFoundException("Summon not found: " + slug));
        SummonVariant variant = findDefaultVariant(summon.getId())
                .orElseThrow(() -> new NotFoundException("Default summon variant not found: " + summon.getId()));
        return toPublicResponse(summon, variant);
    }

    @Transactional(readOnly = true)
    public SummonCalendarFeedResponse getCalendar() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        List<CalendarEntry> entries = loadPublishedCalendarEntries();

        List<SummonPublicResponse> current = entries.stream()
                .filter(entry -> !entry.variant().getStartsAt().isAfter(now))
                .filter(entry -> entry.variant().getEndsAt().isAfter(now))
                .sorted(Comparator.comparing(entry -> entry.variant().getEndsAt()))
                .map(entry -> toPublicResponse(entry.summon(), entry.variant()))
                .toList();

        List<SummonPublicResponse> upcoming = entries.stream()
                .filter(entry -> entry.variant().getStartsAt().isAfter(now))
                .sorted(Comparator.comparing(
                        (CalendarEntry entry) -> entry.variant().getStartsAt()
                ).reversed())
                .map(entry -> toPublicResponse(entry.summon(), entry.variant()))
                .toList();

        return new SummonCalendarFeedResponse(current, upcoming);
    }

    @Transactional(readOnly = true)
    public CatalogPageResponse<SummonPublicResponse> getHistory(int page, Integer size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(
                Math.max(size == null ? DEFAULT_HISTORY_PAGE_SIZE : size, 1),
                MAX_HISTORY_PAGE_SIZE
        );
        OffsetDateTime now = OffsetDateTime.now(clock);

        List<CalendarEntry> history = loadPublishedCalendarEntries().stream()
                .filter(entry -> !entry.variant().getEndsAt().isAfter(now))
                .sorted(Comparator.comparing(
                        (CalendarEntry entry) -> entry.variant().getEndsAt()
                ).reversed())
                .toList();

        int fromIndex = Math.min(normalizedPage * normalizedSize, history.size());
        int toIndex = Math.min(fromIndex + normalizedSize, history.size());
        List<SummonPublicResponse> items = history.subList(fromIndex, toIndex).stream()
                .map(entry -> toPublicResponse(entry.summon(), entry.variant()))
                .toList();

        return CatalogPageResponse.from(new PageImpl<>(
                items,
                PageRequest.of(normalizedPage, normalizedSize),
                history.size()
        ));
    }

    @Transactional(readOnly = true)
    public SummonPublicResponse getCalendarVariant(UUID variantId) {
        SummonVariant variant = variantRepository.findByIdAndVariantTypeAndStatus(
                        variantId,
                        SummonVariantType.SCHEDULED,
                        ContentStatus.READY
                )
                .orElseThrow(() -> new NotFoundException("Calendar summon not found: " + variantId));
        Summon summon = summonRepository.findById(variant.getSummonId())
                .orElseThrow(() -> new NotFoundException("Calendar summon not found: " + variantId));
        return toPublicResponse(summon, variant);
    }

    private java.util.Optional<SummonVariant> findDefaultVariant(UUID summonId) {
        return variantRepository.findAllBySummonId(summonId).stream()
                .filter(variant -> variant.getVariantType() == SummonVariantType.DEFAULT)
                .filter(variant -> variant.getStatus() == ContentStatus.READY)
                .findFirst();
    }

    private List<CalendarEntry> loadPublishedCalendarEntries() {
        List<SummonVariant> variants = variantRepository.findAllByVariantTypeAndStatus(
                SummonVariantType.SCHEDULED,
                ContentStatus.READY
        );
        Map<UUID, Summon> summonsById = summonRepository.findAllById(
                        variants.stream().map(SummonVariant::getSummonId).collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(Summon::getId, Function.identity()));

        return variants.stream()
                .filter(variant -> summonsById.containsKey(variant.getSummonId()))
                .map(variant -> new CalendarEntry(summonsById.get(variant.getSummonId()), variant))
                .toList();
    }

    private SummonPublicResponse toPublicResponse(Summon summon, SummonVariant variant) {
        List<SummonProbability> probabilities = probabilityRepository
                .findAllByVariantIdOrderByPositionAsc(variant.getId());
        Map<UUID, SummonProbabilityDefinition> definitionsById = definitionRepository.findAllById(
                        probabilities.stream()
                                .map(SummonProbability::getDefinitionId)
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                ).stream()
                .collect(Collectors.toMap(SummonProbabilityDefinition::getId, Function.identity()));
        Map<UUID, List<SummonHeroPreviewResponse>> heroesByProbability = loadHeroPreviews(probabilities);

        List<SummonPublicProbabilityResponse> probabilityResponses = probabilities.stream()
                .map(probability -> {
                    SummonProbabilityDefinition definition = definitionsById.get(probability.getDefinitionId());
                    if (definition == null) {
                        throw new IllegalStateException(
                                "Probability definition not found: " + probability.getDefinitionId()
                        );
                    }
                    return new SummonPublicProbabilityResponse(
                            probability.getId(),
                            new SummonProbabilityDefinitionResponse(
                                    definition.getId(),
                                    definition.getNameJson(),
                                    definition.getDescriptionJson()
                            ),
                            probability.getKind(),
                            probability.getValuePercent(),
                            probability.getPosition(),
                            probability.isUserSelectionEnabled(),
                            probability.getDefaultSelectedHeroCount(),
                            heroesByProbability.getOrDefault(probability.getId(), List.of())
                    );
                })
                .toList();

        return new SummonPublicResponse(
                summon.getId(),
                summon.getSlug(),
                summon.getNameJson(),
                summon.getDescriptionJson(),
                mediaUrlResolver.resolveUrl(summon.getImageBucket(), summon.getImageObjectKey()),
                mediaUrlResolver.resolveUrl(
                        summon.getBackgroundImageBucket(),
                        summon.getBackgroundImageObjectKey()
                ),
                new SummonPublicVariantResponse(
                        variant.getId(),
                        variant.getVariantType(),
                        variant.getStartsAt(),
                        variant.getEndsAt(),
                        probabilityResponses
                )
        );
    }

    private Map<UUID, List<SummonHeroPreviewResponse>> loadHeroPreviews(
            List<SummonProbability> probabilities
    ) {
        List<UUID> probabilityIds = probabilities.stream()
                .filter(SummonProbability::isUserSelectionEnabled)
                .map(SummonProbability::getId)
                .toList();
        if (probabilityIds.isEmpty()) {
            return Map.of();
        }

        List<SummonHero> links = summonHeroRepository.findAllByIdProbabilityIdIn(probabilityIds);
        Set<Long> heroIds = links.stream()
                .map(link -> link.getId().getHeroId())
                .collect(Collectors.toSet());
        Map<Long, Hero> heroesById = heroRepository.findAllById(heroIds).stream()
                .filter(hero -> hero.getStatus() == ContentStatus.READY)
                .collect(Collectors.toMap(Hero::getId, Function.identity()));
        Map<Long, Rarity> raritiesById = rarityRepository.findAllById(
                        heroesById.values().stream().map(Hero::getRarityId).collect(Collectors.toSet())
                ).stream()
                .collect(Collectors.toMap(Rarity::getId, Function.identity()));

        return links.stream()
                .filter(link -> heroesById.containsKey(link.getId().getHeroId()))
                .collect(Collectors.groupingBy(
                        link -> link.getId().getProbabilityId(),
                        Collectors.collectingAndThen(Collectors.toList(), probabilityLinks -> probabilityLinks.stream()
                                .map(link -> heroesById.get(link.getId().getHeroId()))
                                .sorted(Comparator.comparing(Hero::getId))
                                .map(hero -> {
                                    Rarity rarity = raritiesById.get(hero.getRarityId());
                                    return new SummonHeroPreviewResponse(
                                            hero.getId(),
                                            hero.getSlug(),
                                            hero.getNameJson(),
                                            mediaUrlResolver.resolveUrl(
                                                    hero.getPreviewBucket(),
                                                    hero.getPreviewObjectKey()
                                            ),
                                            rarity == null ? null : rarity.getStars()
                                    );
                                })
                                .toList())
                ));
    }

    private record CalendarEntry(Summon summon, SummonVariant variant) {
    }
}
