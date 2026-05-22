package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.OutfitterForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.OutfitterHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.OutfitterPageResponse;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutfitterPublicService {

    private static final String VISITING_OUTFITTER_TITLE_FRAGMENT = "Visiting Outfitter";
    private static final int VISITING_OUTFITTER_MONTHS = 18;

    private final HeroRepository heroRepository;
    private final PublicationRepository publicationRepository;
    private final HeroPublicResponseConverter heroPublicResponseConverter;
    private final Clock clock;

    public OutfitterPageResponse getAvailableHeroes(int page, int size, HeroLanguage language) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 24);
        LocalDate today = LocalDate.now(clock);
        LocalDate eligibleReleaseDate = today.minusMonths(VISITING_OUTFITTER_MONTHS);

        Page<OutfitterHeroResponse> heroPage = heroRepository.findReadyOutfitterHeroes(
                language.getJsonKey(),
                eligibleReleaseDate,
                PageRequest.of(normalizedPage, normalizedSize)
        ).map(heroPublicResponseConverter::toOutfitterResponse);

        return new OutfitterPageResponse(
                resolveSuggestedPreviousEventDate(),
                heroPage.getContent(),
                heroPage.getNumber(),
                heroPage.getSize(),
                heroPage.getTotalElements(),
                heroPage.getTotalPages(),
                heroPage.hasNext()
        );
    }

    public OutfitterForecastResponse getForecast(LocalDate targetDate, LocalDate previousEventDate, HeroLanguage language) {
        LocalDate suggestedPreviousEventDate = resolveSuggestedPreviousEventDate();
        LocalDate effectivePreviousEventDate = previousEventDate != null
                ? previousEventDate
                : suggestedPreviousEventDate;

        if (targetDate == null) {
            throw new BusinessValidationException("targetDate is required");
        }

        if (effectivePreviousEventDate != null && effectivePreviousEventDate.isAfter(targetDate)) {
            throw new BusinessValidationException("previousEventDate must be before or equal to targetDate");
        }

        LocalDate previousComparisonDate = effectivePreviousEventDate != null
                ? effectivePreviousEventDate
                : targetDate.minusMonths(3);

        List<OutfitterHeroResponse> newlyAvailableHeroes = heroRepository.findReadyOutfitterHeroesReleasedBetween(
                        language.getJsonKey(),
                        previousComparisonDate.minusMonths(VISITING_OUTFITTER_MONTHS),
                        targetDate.minusMonths(VISITING_OUTFITTER_MONTHS)
                ).stream()
                .map(heroPublicResponseConverter::toOutfitterResponse)
                .toList();

        return new OutfitterForecastResponse(
                suggestedPreviousEventDate,
                previousComparisonDate,
                targetDate,
                newlyAvailableHeroes
        );
    }

    private LocalDate resolveSuggestedPreviousEventDate() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        return publicationRepository.findLatestPublishedAtByEnglishTitleContainingSince(
                        VISITING_OUTFITTER_TITLE_FRAGMENT,
                        now.minusMonths(3)
                )
                .map(this::toLocalDate)
                .orElse(null);
    }

    private LocalDate toLocalDate(Instant publishedAt) {
        return publishedAt.atZone(clock.getZone()).toLocalDate();
    }
}
