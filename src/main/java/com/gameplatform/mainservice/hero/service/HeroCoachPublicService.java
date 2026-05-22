package com.gameplatform.mainservice.hero.service;

import com.gameplatform.mainservice.exception.exceptions.BusinessValidationException;
import com.gameplatform.mainservice.hero.converter.HeroPublicResponseConverter;
import com.gameplatform.mainservice.hero.domain.enums.HeroLanguage;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachForecastResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachHeroResponse;
import com.gameplatform.mainservice.hero.dto.response.HeroCoachPageResponse;
import com.gameplatform.mainservice.hero.repository.HeroRepository;
import com.gameplatform.mainservice.publication.repository.PublicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HeroCoachPublicService {

    private static final String HERO_COACH_TITLE_FRAGMENT = "Hero Coach";
    private static final int HERO_COACH_DAYS = 730;

    private final HeroRepository heroRepository;
    private final PublicationRepository publicationRepository;
    private final HeroPublicResponseConverter heroPublicResponseConverter;
    private final Clock clock;

    public HeroCoachPageResponse getAvailableHeroes(int page, int size, HeroLanguage language) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 24);
        LocalDate today = LocalDate.now(clock);
        LocalDate eligibleReleaseDate = today.minusDays(HERO_COACH_DAYS);

        Page<HeroCoachHeroResponse> heroPage = heroRepository.findReadyHeroCoachHeroes(
                language.getJsonKey(),
                eligibleReleaseDate,
                PageRequest.of(normalizedPage, normalizedSize)
        ).map(heroPublicResponseConverter::toHeroCoachResponse);

        return new HeroCoachPageResponse(
                resolveSuggestedPreviousEventDate(),
                heroPage.getContent(),
                heroPage.getNumber(),
                heroPage.getSize(),
                heroPage.getTotalElements(),
                heroPage.getTotalPages(),
                heroPage.hasNext()
        );
    }

    public HeroCoachForecastResponse getForecast(LocalDate targetDate, LocalDate previousEventDate, HeroLanguage language) {
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

        List<HeroCoachHeroResponse> newlyAvailableHeroes = heroRepository.findReadyHeroCoachHeroesReleasedBetween(
                        language.getJsonKey(),
                        previousComparisonDate.minusDays(HERO_COACH_DAYS),
                        targetDate.minusDays(HERO_COACH_DAYS)
                ).stream()
                .map(heroPublicResponseConverter::toHeroCoachResponse)
                .toList();

        return new HeroCoachForecastResponse(
                suggestedPreviousEventDate,
                previousComparisonDate,
                targetDate,
                newlyAvailableHeroes
        );
    }

    private LocalDate resolveSuggestedPreviousEventDate() {
        OffsetDateTime now = OffsetDateTime.now(clock);
        return publicationRepository.findLatestPublishedAtByEnglishTitleContainingSince(
                        HERO_COACH_TITLE_FRAGMENT,
                        now.minusMonths(3)
                )
                .map(OffsetDateTime::toLocalDate)
                .orElse(null);
    }
}
